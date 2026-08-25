import sqlite3
import os

db_path = r"c:\DermaCareAI_New\backend\dermacare.db"

def fix_db():
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Check if address column exists
    cursor.execute("PRAGMA table_info(doctors)")
    columns = [info[1] for info in cursor.fetchall()]
    
    if "address" not in columns:
        print("Adding address column...")
        cursor.execute("ALTER TABLE doctors ADD COLUMN address TEXT")
    
    if "latitude" not in columns:
        print("Adding latitude column...")
        cursor.execute("ALTER TABLE doctors ADD COLUMN latitude FLOAT")
        
    if "longitude" not in columns:
        print("Adding longitude column...")
        cursor.execute("ALTER TABLE doctors ADD COLUMN longitude FLOAT")
        
    conn.commit()
    conn.close()
    print("Database fixed.")

if __name__ == "__main__":
    fix_db()
