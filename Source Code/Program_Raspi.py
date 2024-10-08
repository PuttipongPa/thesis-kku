# final.py
from dronekit import connect, VehicleMode, Command, LocationGlobalRelative, LocationGlobal
from pymavlink import mavutil
from dronekit_sitl import SITL
import time
import mysql.connector
import io
import picamera
import boto3
import cv2
import numpy as np
import math
import requests
import json

# Connect to database
db_config = {
    'user': 'root2',
    'password': '7f449104603fc6b60619',
    'host': '203.154.83.233',
    'database': 'gps_data'
}
cnx = mysql.connector.connect(**db_config)
cursor = cnx.cursor()

connection_string = "/dev/ttyACM0"  # Use this for USB connectionn | show drivercommand : ls /dev
print("conn pixhawk suc")

baud_rate = 57600
print("Connecting to the vehicle on: %s" % connection_string)
vehicle = connect(connection_string, baud=baud_rate, wait_ready=True, heartbeat_timeout=120)
print("Vehicle conn Suc")


# Connect to pixhawk
connection_string = "/dev/ttyACM0"  # Use this for USB connectionn | show drivercommand : ls /dev
baud_rate = 57600
print("Connecting to the vehicle on: %s" % connection_string)
vehicle = connect(connection_string, baud=baud_rate, wait_ready=True, heartbeat_timeout=120)
print("Vehicle conn Suc")

def get_gps_data(vehicle):
    print("GPS: %s" % vehicle.location.global_relative_frame)
    print("Satellites visible: %s" % vehicle.gps_0.satellites_visible)
    print("GPS Fix: %s" % vehicle.gps_0.fix_type)

def save_image_to_db(cnx, cursor, image_bytes, vehicle):
    print("Saving image to database")

    query = '''
        INSERT INTO gps_records (latitude, longitude, altitude, satellites_visible, gps_fix, image_data)
        VALUES (%s, %s, %s, %s, %s, %s)
    '''
    location = vehicle.location.global_relative_frame
    satellites_visible = vehicle.gps_0.satellites_visible
    gps_fix = vehicle.gps_0.fix_type
    data = (location.lat, location.lon, location.alt, satellites_visible, gps_fix, image_bytes)
    cursor.execute(query, data)
    cnx.commit()

    print("Image saved to database")

def detect_fire(image_bytes):
    # Load the image
    image = cv2.imread('image_bytes.jpg')

    # Convert the image to the HSV color space
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Adjust the saturation channel to enhance fire-like colors
    hsv_image[:, :, 1] = np.clip(hsv_image[:, :, 1] * 1.5, 0, 255).astype(np.uint8)  # Increase         saturation within valid range

    # Convert the image back to the BGR color space
    image_bytes = cv2.cvtColor(hsv_image, cv2.COLOR_HSV2BGR)

    # Save the enhanced image
    cv2.imwrite('enhanced_image.jpg', enhanced_image)
    rekognition_client = boto3.client('rekognition')

    response = rekognition_client.detect_labels(
        Image={'Bytes': image_bytes},
        MaxLabels=10
    )

    for label in response['Labels']:
        if label['Name'].lower() == 'fire':
            return True

    return False

# Calculate the distance between two points in 3D space
def distance_between_points(lat1, lon1, alt1, lat2, lon2, alt2):
    d_lat = math.radians(lat2 - lat1)
    d_lon = math.radians(lon2 - lon1)
    a = (math.sin(d_lat / 2) * math.sin(d_lat / 2) +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
         math.sin(d_lon / 2) * math.sin(d_lon / 2))
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    d = 6371 * c * 1000  # Distance in meters
    d_alt = alt2 - alt1  # Altitude difference in meters
    return math.sqrt(d * d + d_alt * d_alt)

# Delete a waypoint from the database
def delete_waypoint(cnx, sequence):
    with cnx.cursor() as cursor:
        sql = "DELETE FROM `waypoints` WHERE `sequence` = %s"
        cursor.execute(sql, (sequence,))
        cnx.commit()

# Add return-to-launch (RTL) command
def return_to_launch():
    cmds = vehicle.commands
    cmds.upload()
    cmds.clear()
    rtl_cmd = Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH, 0, 0, 0, 0, 0, 0, 0, 0, 0)


    cmds.add(rtl_cmd)
    cmds.upload()

def takeoff(vehicle):
    # Arm the vehicle
    vehicle.mode = VehicleMode("GUIDED")
    vehicle.armed = True
    while not vehicle.armed:
        print('Waiting for arming...')
        time.sleep(1)
    print('Vehicle armed')

    # Take off to 5 meters altitude
    target_altitude = 5
    print('Taking off to %d meters' % target_altitude)
    vehicle.simple_takeoff(5)

    while True:
        altitude = vehicle.location.global_relative_frame.alt
        if altitude >= target_altitude * 0.95:
            print('Reached target altitude')
            break
        time.sleep(1)

def check_distance_waypoint(vehicle, waypoint, cnx):
    current_location = vehicle.location.global_relative_frame
    distance = current_location.distance_to(waypoint)

    threshold = 1  # Set the threshold distance in meters

    if distance < threshold:
        print(f"Waypoint {waypoint[0]} reached. Deleting from the database.")
        delete_waypoint(cnx, waypoint[0])
        return False
    return True

def Gotowaypoint(cursor,starterpoint,cnx):
    # Define the SQL query to fetch the waypoints from the database
    sql = "SELECT id, latitude, longitude, altitude FROM waypoints ORDER BY id"

    # Execute the query and fetch the results
    cursor.execute(sql)
    waypoints = cursor.fetchall()

    # Convert the fetched waypoints to LocationGlobalRelative objects
    waypoints_rel = [LocationGlobalRelative(wp[1], wp[2], wp[3]) for wp in waypoints]

    # Navigate to each waypoint in sequence
    for waypoint in waypoints_rel:
        print('Going to waypoint: %s' % waypoint)
        vehicle.simple_goto(waypoint)
       #while check_distance_waypoint(vehicle, waypoint, cnx):
        time.sleep(3)
        start_time = time.time()
        while (time.time() - start_time) < 20:
            if detect_fire(image_bytes):
                print("Fire detected!")
                save_image_to_db(cnx, cursor, image_bytes, vehicle)
            else:
                print("No fire detected.")
            time.sleep(1)

    vehicle.simple_goto(starterpoint)
    vehicle.mode = VehicleMode('LAND')
    while not vehicle.mode.name == 'LAND':
        print('Waiting for mode change...')
        time.sleep(1)
    print('Vehicle landing')

#......................................................

print("start camera")
with picamera.PiCamera() as camera:
    camera.resolution = (640, 480)
    camera.framerate = 24

    starterpoint = vehicle.location.global_relative_frame
    print("starterpoint:", starterpoint)

    try:
        while True:
            print("Start....Function")
            #get_gps_data(vehicle)
            stream = io.BytesIO()
            camera.capture(stream, format='jpeg')
            stream.seek(0)
            image_bytes = stream.read()

            takeoff(vehicle)
            print("takeoff suc")

            starterpoint = vehicle.location.global_relative_frame
            print("starterpoint:", starterpoint)

            # waypoints from the database
            waypoints = Gotowaypoint(cursor,starterpoint,cnx)
            print("waypoints:", waypoints)

        time.sleep(1)

    except KeyboardInterrupt:
        vehicle.simple_goto(starterpoint)
        vehicle.mode = VehicleMode('LAND')
        while not vehicle.mode.name == 'LAND':
            print('Waiting for mode change...')
            time.sleep(1)
        print('Vehicle landing')

# Close the vehicle
vehicle.close()
print('Connection closed')
