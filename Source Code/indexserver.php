<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
header("Access-Control-Allow-Methods: GET");

// Replace the values below with your database credentials
$servername = "203.154.83.233";
$username = "root2";
$password = "7f449104603fc6b60619";
$dbname = "gps_data";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Check if the HTTP method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Retrieve the JSON data from the POST request
    $data = json_decode(file_get_contents("php://input"), true);

    $latitude = $data["latitude"];
    $longitude = $data["longitude"];
    $altitude = $data["altitude"];

    $stmt = $conn->prepare("INSERT INTO waypoints (latitude, longitude, altitude) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $latitude, $longitude, $altitude);

    if ($stmt->execute()) {
        http_response_code(200);
        echo json_encode(array("message" => "Waypoint added successfully."));
    } else {
        http_response_code(500);
        echo json_encode(array("message" => "Unable to add waypoint."));
    }

    $stmt->close();
    $conn->close();
} else {

    $sql = "SELECT * FROM gps_records ORDER BY timestamp DESC";
    $result = $conn->query($sql);

    $waypoints = array();

    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $waypoints[] = array(
                "id" => $row["id"],
                "timestamp" => $row["timestamp"],
                "latitude" => $row["latitude"],
                "longitude" => $row["longitude"],
                "altitude" => $row["altitude"],
                "satellites_visible" => $row["satellites_visible"],
                "gps_fix" => $row["gps_fix"],
                "image_data" => base64_encode($row['image_data'])

            );
        }
    }

    echo json_encode($waypoints);

    $conn->close();
}
?>
