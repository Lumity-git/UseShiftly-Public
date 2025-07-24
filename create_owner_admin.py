#!/usr/bin/env python3
import requests
import getpass
import sys

API_BASE_URL = "https://useshiftly.com/api"  # Change if backend runs elsewhere

def prompt(msg, required=True, hide=False):
    while True:
        if hide:
            val = getpass.getpass(msg+': ')
        else:
            val = input(msg+': ')
        if not required or val.strip():
            return val.strip()
        print("This field is required.")

def main():
    print("--- Create New Building Owner Admin ---")
    # Authenticate as existing admin
    admin_email = prompt("Admin email for authentication")
    admin_password = prompt("Admin password", hide=True)
    # Login to get token
    resp = requests.post(f"{API_BASE_URL}/auth/login", json={"email": admin_email, "password": admin_password})
    if resp.status_code != 200:
        print("Login failed!", resp.text)
        sys.exit(1)
    token = resp.json().get("token")
    if not token:
        print("No token received!")
        sys.exit(1)
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    # Gather new admin info
    print("\n--- New Owner Admin Info ---")
    email = prompt("Owner admin email")
    first_name = prompt("First name")
    last_name = prompt("Last name")
    phone = prompt("Phone number")
    dob = prompt("Date of birth (YYYY-MM-DD)")
    address = prompt("Address")
    emergency_name = prompt("Emergency contact name")
    emergency_relation = prompt("Emergency contact relation")
    emergency_phone = prompt("Emergency contact phone")
    building_name = prompt("Building name")
    department_name = prompt("Department name (e.g. Front Desk)")
    # Set a temp password
    temp_password = prompt("Temporary password (will require change on first login)")
    # Create or get building
    b_resp = requests.get(f"{API_BASE_URL}/buildings/by-name/{building_name}", headers=headers)
    if b_resp.status_code == 200 and b_resp.json():
        building_id = b_resp.json().get("id")
    else:
        b_resp = requests.post(f"{API_BASE_URL}/buildings", json={"name": building_name}, headers=headers)
        if b_resp.status_code != 200:
            print("Failed to create building:", b_resp.text)
            sys.exit(1)
        building_id = b_resp.json().get("id")
    # Create or get department
    d_resp = requests.get(f"{API_BASE_URL}/departments/by-name/{department_name}", headers=headers)
    if d_resp.status_code == 200 and d_resp.json():
        department_id = d_resp.json().get("id")
    else:
        d_resp = requests.post(f"{API_BASE_URL}/departments", json={"name": department_name}, headers=headers)
        if d_resp.status_code != 200:
            print("Failed to create department:", d_resp.text)
            sys.exit(1)
        department_id = d_resp.json().get("id")
    # Create the admin user
    user_data = {
        "email": email,
        "password": temp_password,
        "firstName": first_name,
        "lastName": last_name,
        "role": "ADMIN",
        "departmentId": department_id,
        "buildingId": building_id,
        "active": True,
        "mustChangePassword": True,
        "phoneNumber": phone,
        "dateOfBirth": dob,
        "address": address,
        "emergencyContactName": emergency_name,
        "emergencyContactRelation": emergency_relation,
        "emergencyContactPhone": emergency_phone
    }
    u_resp = requests.post(f"{API_BASE_URL}/employees", json=user_data, headers=headers)
    if u_resp.status_code == 200:
        print("\nSuccess! Owner admin created:", email)
        print("Temporary password:", temp_password)
    else:
        print("Failed to create admin:", u_resp.status_code, u_resp.text)

if __name__ == "__main__":
    main()
