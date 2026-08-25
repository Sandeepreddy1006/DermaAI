from database import SessionLocal, engine
import models
from auth import get_password_hash

def create_test_user():
    db = SessionLocal()
    
    # Check if user already exists
    email = "test@gmail.com"
    db_user = db.query(models.User).filter(models.User.email == email).first()
    
    if db_user:
        print(f"User {email} already exists.")
    else:
        hashed_password = get_password_hash("password123")
        new_user = models.User(
            full_name="Test User",
            email=email,
            hashed_password=hashed_password
        )
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        print(f"Test user created: {email} / password123")
    
    db.close()

if __name__ == "__main__":
    # Create tables if they don't exist
    models.Base.metadata.create_all(bind=engine)
    create_test_user()
