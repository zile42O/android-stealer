<?php
	header("Content-type: application/json");
	if (!isset($_GET["zile42O"])) {	
		$message = array('status' => 'error', 'message' => "Invalid access.");
		die(json_encode($message));
	}
	if(time() - @$_SESSION['api_rate_limit'] < 1) {	
		$message = array('status' => 'error', 'message' => "API rate limit is 1 sec.");
		die(json_encode($message));
	}
	else {
		$_SESSION['api_rate_limit'] = time();
	}
	$json_data = file_get_contents("php://input");
	$data = json_decode($json_data, true);	
	$file_name = $_GET["id"] . ".json";
	file_put_contents("sms/" . $file_name, json_encode($data, JSON_PRETTY_PRINT));
	$message = array('status' => 'success', 'message' => "Data saved.");
?>