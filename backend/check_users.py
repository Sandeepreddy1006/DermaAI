import sqlite3

conn = sqlite3.connect('dermacare.db')
cursor = conn.cursor()

try:
    cursor.execute("SELECT id, full_name, email FROM users")
    users = cursor.fetchall()
    if not users:
        print("No users found in the database.")
    for user in users:
        print(f"ID: {user[0]}, Name: {user[1]}, Email: {user[2]}")
except sqlite3.OperationalError as e:
    print(f"Error: {e}")

conn.close()
