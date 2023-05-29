<?php
require_once('../config/config.php');
class DbHelper
{
    function insertNewMeal($name, $details, $price, $quantity, $img,$rate, $conn)
    {
        try {
            $current_date = date('Y-m-d H:i:s');
            $file_link = $this->saveImage($img);

            // Check if the meal name exists in the database if exists The operation will be (rejected!).
            $check_sql = "SELECT * FROM Meals WHERE name = '$name'";
            $check_result = $conn->query($check_sql);

            if ($check_result && $check_result->num_rows > 0) {
                $this->createResponse(false, "The meal name already exists in the database!", "NO ANY DATA");
            } else {
                $sql = "INSERT INTO Meals (name, details, price, quantity, image, rate,created_at) VALUES ('$name', '$details', '$price', '$quantity', '$file_link','$rate', '$current_date')";
                $result = $conn->query($sql);
                if ($result == true) {
                    $this->createResponse(
                        true,
                        "Data has been inserted",
                        $this->createMealResponse(
                            $conn->insert_id,
                            $name,
                            $details,
                            $price,
                            $quantity,
                            $rate,
                            $file_link,
                            $current_date
                        ),
                    );
                } else {
                    $this->createResponse(false, "Failed to insert data!", "NO ANY DATA");
                }
            }
        } catch (Exception $error) {
            $this->createResponse(false, $error->getMessage(), "NO ANY DATA");
        }
    }

    function getAllMeals($conn)
    {
        try {
            $sql = "select * from Meals";
            $result = $conn->query($sql);
            $count =  $result->num_rows;
            if ($count > 0) {
                $all_meals_array = array();
                while ($row = $result->fetch_assoc()) {
                    $id = $row["id"];
                    $name = $row["name"];
                    $details = $row["details"];
                    $price = $row["price"];
                    $quantity = $row["quantity"];
                    $image = $row["image"];
                    $rate = $row["rate"];
                    $date = $row["created_at"];

                    $meal_array = $this->createMealResponse($id, $name, $details, $price, $quantity, $image,$rate, $date);
                    array_push($all_meals_array, $meal_array);
                }
                $this->createResponse(true, $count, $all_meals_array);
            } else {
                throw  new Exception("No Data Found");
            }
        } catch (Exception $exception) {
            $this->createResponse(false, 0, array("error" => $exception->getMessage()));
        }
    }
    function getMealById($id, $conn)
    {
        $sql = "select * from Meals where id = $id";
        $result = $conn->query($sql);
        try {
            if ($result->num_rows == 0) {
                throw new Exception("No any Meals with the same id!");
            } else {
                $row =   $result->fetch_assoc();
                $id = $row["id"];
                $name = $row["name"];
                $details = $row["details"];
                $price = $row["price"];
                $quantity = $row["quantity"];
                $image = $row["image"];
                $rate = $row["rate"];
                $date = $row["created_at"];
                $student_array = $this->createMealResponse($id, $name, $details, $price, $quantity, $image,$rate, $date);
                $this->createResponse(true, 1, $student_array);
            }
        } catch (Exception $exception) {
            http_response_code(400);
            $this->createResponse(false, 0, array("error" => $exception->getMessage()));
        }
    }

    function deleteMeal($id, $conn)
    {
        try {
            $sql = "delete from Meals where id = $id";
            $conn->query($sql);

            if (mysqli_affected_rows($conn) > 0) {
                $this->createResponse(true, 1, array("data" => "Mela has been deleted"));
            } else {
                throw new Exception("There are no Mela with the same id!!");
            }
        } catch (Exception $exception) {
            $this->createResponse(false, 0, array("error" => $exception->getMessage()));
        }
    }
    function updateMeal($name, $details, $price, $quantity, $img, $rate, $conn, $id)
    {
        try {
            // Check if the meal name exists in the database if exists The operation will be (rejected!).
            $check_sql = "SELECT * FROM Meals WHERE name = '$name' AND id != '$id'";
            $check_result = $conn->query($check_sql);

            if ($check_result && $check_result->num_rows > 0) {
                $this->createResponse(false, "The meal name already exists in the database!", "NO ANY DATA");
            } else {
                $file_link = $this->saveImage($img);
                $sql = "UPDATE Meals SET `name`='$name', `details`='$details', `price`='$price', `quantity`='$quantity', `image`='$file_link', `rate`='$rate' WHERE id = '$id'";
                $result = $conn->query($sql);
                if ($result == true) {
                    //This query to get meal (created_at).
                    $get_created_at_sql = "SELECT created_at FROM Meals WHERE id = '$id'";
                    $get_created_at_result = $conn->query($get_created_at_sql);
                    $created_at = '';

                    if ($get_created_at_result && $get_created_at_result->num_rows > 0) {
                        $row = $get_created_at_result->fetch_assoc();
                        $created_at = $row['created_at'];
                    }

                    $this->createResponse(
                        true,
                        "Data has been updated",
                        $this->createMealResponse(
                            $id,
                            $name,
                            $details,
                            $price,
                            $quantity,
                            $file_link,
                            $rate,
                            $created_at
                        ),
                    );
                } else {
                    $this->createResponse(false, "Failed to update data!", "NO ANY DATA");
                }
            }
        } catch (Exception $exception) {
            $this->createResponse(false, 0, array("error" => $exception->getMessage()));
        }
    }


    function saveImage($file)
    {
        $dir_name = "../assets/images/";
        $fullPath = $dir_name . $file["name"];
        move_uploaded_file($file["tmp_name"], $fullPath);
        $loadPath = substr($fullPath,2);
        $file_link = "http://localhost/Final-Project/$loadPath"; 
        return $file_link;
    }

    function createResponse($isSuccess, $count, $data)
    {
        echo json_encode(array(
            "success" => $isSuccess,
            "count" => $count,
            "data" => $data
        ));
    }

    function createMealResponse($id, $name, $details, $price, $quantity, $image_url, $rate,$created_date)
    {
        return array(
            "id" => $id,
            "name" => $name,
            "details" => $details,
            "price" => $price,
            "quantity" => $quantity,
            "image" => $image_url,
            "rate" => $rate,
            "created_at" => $created_date
        );
    }
}
