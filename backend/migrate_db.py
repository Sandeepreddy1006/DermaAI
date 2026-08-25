import sqlite3

def migrate():
    conn = sqlite3.connect('dermacare.db')
    cursor = conn.cursor()
    
    try:
        print("Adding 'precautions' column to 'analyses' table...")
        cursor.execute("ALTER TABLE analyses ADD COLUMN precautions TEXT")
        conn.commit()
        print("Migration successful!")
    except sqlite3.OperationalError as e:
        if "duplicate column name" in str(e).lower():
            print("Column 'precautions' already exists.")
        else:
            print(f"Migration failed: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    migrate()
