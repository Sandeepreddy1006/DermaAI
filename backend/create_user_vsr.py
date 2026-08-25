from database import SessionLocal, engine
import models
from auth import get_password_hash

def create_user(name, email, password):
    db = SessionLocal()
    
    # Check if user already exists
    db_user = db.query(models.User).filter(models.User.email == email).first()
    
    if db_user:
        print(f"User {email} already exists.")
    else:
        hashed_password = get_password_hash(password)
        new_user = models.User(
            full_name=name,
            email=email,
            hashed_password=hashed_password
        )
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        print(f"User created: {email} / {password}")
    
    db.close()

if __name__ == "__main__":
    models.Base.metadata.create_all(bind=engine)
    create_user("User", "vsr10062005@gmail.com", "password123")
