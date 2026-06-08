import os
import zipfile
import shutil
import tempfile
import xml.etree.ElementTree as ET
from datetime import datetime
from xml.dom import minidom
from fastapi import FastAPI, UploadFile, File, HTTPException, Header, Depends, Request
from fastapi.staticfiles import StaticFiles
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.responses import FileResponse
from pymongo import MongoClient

app = FastAPI(title="CARET P2 HTTPS Repository Server")

# --- MongoDB Configuration ---
# Replace with your actual connection string
MONGO_URI = "" 
DB_NAME = "db_caret"
COLLECTION_NAME = "plugins"

UPLOAD_DIR = "plugins"
EXPECTED_TOKEN = ""
security = HTTPBearer()

if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

def log_interaction_to_mongo(extension_id: str, version: str):
    """Inserts a record of the upload interaction into MongoDB with debug prints."""
    print(f"---> Attempting to log to MongoDB: {extension_id} v{version}")
    try:
        # We set a 5-second timeout so the app doesn't hang if the DB is down
        client = MongoClient(MONGO_URI, serverSelectionTimeoutMS=5000)
        db = client[DB_NAME]
        collection = db[COLLECTION_NAME]
        
        document = {
            "extension_id": extension_id,
            "version": version,
            "timestamp": datetime.utcnow().isoformat(),
            "action": "plugin_upload",
            "status": "success"
        }
        
        result = collection.insert_one(document)
        print(f"✅ SUCCESS: Document inserted with ID: {result.inserted_id}")
        client.close()
    except Exception as e:
        print(f"❌ MONGODB ERROR: {str(e)}")

@app.get("/plugins/content.xml")
async def get_content_xml():
    path = os.path.join(UPLOAD_DIR, "compositeContent.xml")
    if os.path.exists(path):
        return FileResponse(path)
    raise HTTPException(status_code=404, detail="Metadata not found")

@app.get("/plugins/artifacts.xml")
async def get_artifacts_xml():
    path = os.path.join(UPLOAD_DIR, "compositeArtifacts.xml")
    if os.path.exists(path):
        return FileResponse(path)
    raise HTTPException(status_code=404, detail="Artifacts not found")

app.mount("/plugins", StaticFiles(directory=UPLOAD_DIR), name="plugins")

def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    if credentials.credentials != EXPECTED_TOKEN:
        raise HTTPException(status_code=401, detail="Invalid token")
    return True

def generate_composite_xml(filename, repo_type, locations):
    """Generates XML files with p2 processing instructions."""
    pi_name = "compositeMetadataRepository" if repo_type == "metadata" else "compositeArtifactRepository"
    
    root = ET.Element("repository", {
        "name": "CARET Composite Repository",
        "type": f"org.eclipse.equinox.internal.p2.{repo_type}.repository.Composite{repo_type.capitalize()}Repository",
        "version": "1.0.0"
    })
    
    properties = ET.SubElement(root, "properties", {"size": "2"})
    ET.SubElement(properties, "property", {"name": "p2.timestamp", "value": str(int(datetime.now().timestamp()))})
    ET.SubElement(properties, "property", {"name": "p2.compressed", "value": "false"})
    
    children = ET.SubElement(root, "children", {"size": str(len(locations))})
    for loc in locations:
        ET.SubElement(children, "child", {"location": loc})
    
    xml_str = ET.tostring(root, encoding='utf-8')
    pretty_xml = minidom.parseString(xml_str).toprettyxml(indent="  ")
    
    lines = pretty_xml.split('\n')
    if lines[0].startswith('<?xml'):
        lines.insert(1, f"<?{pi_name} version='1.0.0'?>")
    
    file_path = os.path.join(UPLOAD_DIR, filename)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write('\n'.join(lines))

def refresh_p2_repository():
    """Scans the plugins folder and updates the composite indices."""
    locations = []
    if not os.path.exists(UPLOAD_DIR): return

    for ext_id in os.listdir(UPLOAD_DIR):
        id_path = os.path.join(UPLOAD_DIR, ext_id)
        if os.path.isdir(id_path) and not ext_id.startswith('.'):
            for version in os.listdir(id_path):
                if os.path.isdir(os.path.join(id_path, version)):
                    
                    locations.append(f"{ext_id}/{version}")

    generate_composite_xml("compositeContent.xml", "metadata", locations)
    generate_composite_xml("compositeArtifacts.xml", "artifact", locations)

@app.post("/upload")
async def upload_extension(
    file: UploadFile = File(...),
    x_extension_id: str = Header(...),
    x_extension_version: str = Header(...),
    authenticated: bool = Depends(verify_token)
):
    target_path = os.path.join(UPLOAD_DIR, x_extension_id, x_extension_version)
    
    if os.path.exists(target_path):
        shutil.rmtree(target_path, ignore_errors=True)
            
    os.makedirs(target_path, exist_ok=True)

    temp_fd, temp_path = tempfile.mkstemp(suffix=".zip")
    try:
        with os.fdopen(temp_fd, 'wb') as tmp:
            shutil.copyfileobj(file.file, tmp)
        
        with zipfile.ZipFile(temp_path, 'r') as zip_ref:
            zip_ref.extractall(target_path)
            
        refresh_p2_repository()

        # --- MongoDB Logging ---
        log_interaction_to_mongo(x_extension_id, x_extension_version)

        return {"status": "success", "id": x_extension_id, "version": x_extension_version}
    except Exception as e:
        print(f"❌ UPLOAD ERROR: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_path): os.remove(temp_path)

if __name__ == "__main__":
    import uvicorn
    
    BASE_DIR = os.path.dirname(os.path.abspath(__file__))
    cert_path = os.path.join(BASE_DIR, "cert.pem")
    key_path = os.path.join(BASE_DIR, "key.pem")

    if not os.path.exists(cert_path) or not os.path.exists(key_path):
        print(f"CRITICAL ERROR: Certificates not found in: {BASE_DIR}")
        print("Ensure 'cert.pem' and 'key.pem' are in the same folder as main.py")
    else:
        uvicorn.run(
            "main:app", 
            host="", 
            port=7500, 
            ssl_keyfile=key_path,  
            ssl_certfile=cert_path, 
            reload=True
        )