#!/usr/bin/env python3
"""
Database connection test and data insertion script
"""

import sys
import os
from datetime import datetime

# Add current directory to Python path
sys.path.append(os.path.dirname(__file__))

from app.database import SessionLocal, engine
from app.models import User, HealthCheck, Base
from sqlalchemy import text

def test_db_connection():
    """Database connection test"""
    try:
        print("Testing database connection...")
        
        # Drop and recreate tables
        Base.metadata.drop_all(bind=engine)
        Base.metadata.create_all(bind=engine)
        print("OK: Tables recreated")
        
        # Create session
        db = SessionLocal()
        
        # Simple query test
        result = db.execute(text("SELECT 1 FROM DUAL"))
        print("OK: Database connection successful")
        
        # Check existing data
        user_count = db.query(User).count()
        health_count = db.query(HealthCheck).count()
        print(f"OK: Existing users: {user_count}")
        print(f"OK: Existing health checks: {health_count}")
        
        # Insert test user
        test_user = User(
            username="testuser1",
            email="test1@example.com"
        )
        db.add(test_user)
        db.commit()
        db.refresh(test_user)
        print(f"OK: Test user created - ID: {test_user.id}")
        
        # Insert health check data
        health_check = HealthCheck(
            status="ok"
        )
        db.add(health_check)
        db.commit()
        db.refresh(health_check)
        print(f"OK: Health check data created - ID: {health_check.id}")
        
        # Final data check
        final_user_count = db.query(User).count()
        final_health_count = db.query(HealthCheck).count()
        print(f"OK: Final user count: {final_user_count}")
        print(f"OK: Final health check count: {final_health_count}")
        
        # Query actual data
        all_users = db.query(User).all()
        print("\nAll users:")
        for user in all_users:
            print(f"  - ID: {user.id}, Username: {user.username}, Email: {user.email}, Created: {user.created_at}")
            
        all_health = db.query(HealthCheck).all()
        print("\nAll health checks:")
        for health in all_health:
            print(f"  - ID: {health.id}, Status: {health.status}, Checked: {health.checked_at}")
        
        db.close()
        return True
        
    except Exception as e:
        print(f"ERROR: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = test_db_connection()
    if success:
        print("\nDatabase test SUCCESS!")
    else:
        print("\nDatabase test FAILED!")
        sys.exit(1)