<?php
//
// This is sample configuration file
//
// You can configure phpliteadmin in one of 2 ways:
// 1. Rename current configuration file to phpliteadmin.config.php and change parameters here.
//    You can leave here only your custom settings. All other settings will be set to defaults.
// 2. Change parameters directly in main phpliteadmin.php file
//
// Please see http://code.google.com/p/phpliteadmin/wiki/Configuration for more details

//password to gain access
$password = 'admin';

function addAllDatabases($fail_on_error){ // this is monkeypatching....
	// adds databases from current directory with setting subdirectory = true and from the local db "sqliteDatabases.sqlite3"
	addFilesFromThisDirectory(".");
	addDatabasesFromSqliteDb($fail_on_error);

	global $databases;
	sort($databases);
	//echo "<pre>";print_r($databases);die();
}

//function to scan entire directory tree and subdirectories
function my_dir_tree($dir)
{
	$path = '';
	$stack[] = $dir;
	while($stack)
	{
		$thisdir = array_pop($stack);
		if($dircont = scandir($thisdir))
		{
			$i=0;
			while(isset($dircont[$i]))
			{
				if($dircont[$i] !== '.' && $dircont[$i] !== '..')
				{
					$current_file = $thisdir.DIRECTORY_SEPARATOR.$dircont[$i];
					if(is_file($current_file))
					{
						$path[] = $thisdir.DIRECTORY_SEPARATOR.$dircont[$i];
					}
					elseif (is_dir($current_file))
					{
						$path[] = $thisdir.DIRECTORY_SEPARATOR.$dircont[$i];
						$stack[] = $current_file;
					}
				}
				$i++;
			}
		}
	}
	return $path;
}


function addFilesFromThisDirectory($directory){
	global $databases;
	$arr = my_dir_tree($directory);

	$addedFiles=0;
	$j = sizeof($databases);
	for($i=0; $i<sizeof($arr); $i++) //iterate through all the files in the databases
	{
		if(@!is_file($arr[$i])) continue;
		if(is_readable($arr[$i])){
			$con = file_get_contents($arr[$i], NULL, NULL, 0, 60);
			if(strpos($con, "** This file contains an SQLite 2.1 database **", 0)!==false || strpos($con, "SQLite format 3", 0)!==false)
			{
				$databases[$j]['path'] = $arr[$i];
				$databases[$j]['name'] = $arr[$i];
				$j++;
				$addedFiles++;
			}
		}
	}
	return $addedFiles;
}

function addDatabasesFromSqliteDb($fail_on_error){
	global $databases;
	$db = new PDO("sqlite:sqliteDatabases.sqlite3");
	$query = "SELECT * from databases order by name; ";
	$result = $db->query($query);
	while($thisResult = $result->fetch(PDO::FETCH_ASSOC)){
		$path = $thisResult["path"];

		if( ( @is_readable($path) && @is_file($path) ) || is_writable( dirname($path) )  ){
			$i = sizeof($databases);
			$databases[$i]["path"]=$path;
			$databases[$i]["name"]=$thisResult["name"];
		}
		else if( @is_readable($path) && @is_dir($path) ){
			$addedFiles = addFilesFromThisDirectory( $path );
			if( $addedFiles == 0 && $fail_on_error ){
				die("Warning: I could not add any database from [" . $path . "] either because this folder is empty or there are not enough permissions.<br>\n" .
					"You may continue to <a href='phpliteadmin.php'>SQLite Admin</a>"
					);
			}
		}
		else if( $fail_on_error ){
			die("Error loading database [" . $path . "] from the SQL database.<br>\n" .
				"Please check if the path is valid and you have enough permissions to access the file.<br><br>\n\n" .
				"You may continue to <a href='phpliteadmin.php'>SQLite Admin</a>"
				);
		}
	}
	$result->closeCursor();
	$db = NULL; // close
	//print_r($databases);
}


//directory relative to this file to search for databases (if false, manually list databases in the $databases variable)
$directory = false; // '.';


//whether or not to scan the subdirectories of the above directory infinitely deep
$subdirectories = true;

//if the above $directory variable is set to false, you must specify the databases manually in an array as the next variable
//if any of the databases do not exist as they are referenced by their path, they will be created automatically
$databases = array();
/*
$databases = array(
	array(
		'path'=> 'database1.sqlite',
		'name'=> 'Database 1'
	),
	array(
		'path'=> 'database2.sqlite',
		'name'=> 'Database 2'
	),
);
*/

addAllDatabases(false);

/* ---- Interface settings ---- */

// Theme! If you want to change theme, save the CSS file in same folder of phpliteadmin or in folder "themes"
$theme = 'phpliteadmin.css';

// the default language! If you want to change it, save the language file in same folder of phpliteadmin or in folder "languages"
// More about localizations (downloads, how to translate etc.): http://code.google.com/p/phpliteadmin/wiki/Localization
$language = 'en';

// set default number of rows. You need to relog after changing the number
$rowsNum = 30;

// reduce string characters by a number bigger than 10
$charsNum = 300;


/* ---- Custom functions ---- */

//a list of custom functions that can be applied to columns in the databases
//make sure to define every function below if it is not a core PHP function
$custom_functions = array('md5', 'md5rev', 'sha1', 'sha1rev', 'time', 'mydate', 'strtotime', 'myreplace');

//define all the non-core custom functions
function md5rev($value)
{
	return strrev(md5($value));
}

function sha1rev($value)
{
	return strrev(sha1($value));
}

function mydate($value)
{
	return date('g:ia n/j/y', intval($value));
}

function myreplace($value)
{
	return preg_replace('/[^A-Za-z0-9]/', '', strval($value));
}


/* ---- Advanced options ---- */

//changing the following variable allows multiple phpLiteAdmin installs to work under the same domain.
$cookie_name = 'androidsqlite';

//whether or not to put the app in debug mode where errors are outputted
$debug = false;

// the user is allowed to create databases with only these extensions
$allowed_extensions = array('db','db3','sqlite','sqlite3');

