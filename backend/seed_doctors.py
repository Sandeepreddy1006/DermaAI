from database import SessionLocal, engine
import models

def seed_doctors():
    db = SessionLocal()
    
    # Drop and recreate tables to ensure schema is fresh
    models.Base.metadata.drop_all(bind=engine)
    models.Base.metadata.create_all(bind=engine)
    
    # Chennai-based real hospitals and skin care clinics
    initial_doctors = [
        models.Doctor(
            name="Gleneagles Health City - Dermatology Dept", 
            specialty="Advanced Medical & Aesthetic Dermatology", 
            rating=4.9, 
            distance="15.2 km", 
            address="Perumbakkam, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=12.9184, 
            longitude=80.2057
        ),
        models.Doctor(
            name="Kauvery Hospital - Skin Care Dept", 
            specialty="Dermatology & Skin Care Hospital", 
            rating=4.8, 
            distance="6.4 km", 
            address="Alwarpet, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0336, 
            longitude=80.2520
        ),
        models.Doctor(
            name="Prashanth Super Speciality Hospital", 
            specialty="Skin & Cosmetology Centre", 
            rating=4.7, 
            distance="9.8 km", 
            address="Velachery, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=12.9839, 
            longitude=80.2201
        ),
        models.Doctor(
            name="MGM Healthcare - Dermatology Dept", 
            specialty="Clinical & Autoimmune Skin Care", 
            rating=4.9, 
            distance="4.5 km", 
            address="Nelson Manickam Road, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0728, 
            longitude=80.2291
        ),
        models.Doctor(
            name="Ram Skin Clinic", 
            specialty="Specialized Skin Care & Cosmetology", 
            rating=4.8, 
            distance="11.2 km", 
            address="OMR, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=12.9647, 
            longitude=80.2458
        ),
        models.Doctor(
            name="Dr. Hanan Dermatology Speciality Clinic", 
            specialty="Advanced Skin & Laser Center", 
            rating=4.7, 
            distance="4.8 km", 
            address="Anna Nagar, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0850, 
            longitude=80.2101
        ),
        models.Doctor(
            name="Derma Med Spa", 
            specialty="Skin & Aesthetic Clinic", 
            rating=4.8, 
            distance="3.5 km", 
            address="Nungambakkam, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0617, 
            longitude=80.2423
        ),
        models.Doctor(
            name="Apollo Cosmetic Clinic", 
            specialty="Cosmetic & Dermatology Center", 
            rating=4.9, 
            distance="8.6 km", 
            address="MRC Nagar, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0189, 
            longitude=80.2745
        ),
        models.Doctor(
            name="Render Skin & Hair Clinic", 
            specialty="Clinical & Aesthetic Dermatology", 
            rating=4.8, 
            distance="3.8 km", 
            address="Nungambakkam, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0594, 
            longitude=80.2464
        ),
        models.Doctor(
            name="SIMS Hospital - Dermatology Dept", 
            specialty="Dermatology & Skin Care Specialist", 
            rating=4.9, 
            distance="1.2 km", 
            address="Vadapalani, Chennai", 
            image_url="https://via.placeholder.com/150",
            latitude=13.0489, 
            longitude=80.2089
        )
    ]
    
    db.add_all(initial_doctors)
    db.commit()
    
    # Re-add the user
    from auth import get_password_hash
    test_user = models.User(
        full_name="User",
        email="vsr10062005@gmail.com",
        hashed_password=get_password_hash("password123")
    )
    db.add(test_user)
    db.commit()
    
    db.close()
    print("Local doctors (Chennai) seeded successfully.")

if __name__ == "__main__":
    seed_doctors()
