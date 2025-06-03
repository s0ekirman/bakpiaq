<?php
// konfigurasi db
 $hostname = 'localhost';
 $username = 'root';
 $password = '';

//sesuaikan dengan mysqlnya 
 $database = 'nama db';

//  koneksi database

 $conn = new mysqli($hostname, $username, $password, $database);
 
 if ($conn->connect_error) {
   die("koneksi gagal: ". $conn->connect_error);
 }

 ?>
