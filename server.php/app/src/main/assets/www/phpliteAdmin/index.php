<?php

$config_filename = './phpliteadmin.config.php';
if (is_readable($config_filename)) {
	include_once $config_filename;
}

// we must fail here because if we fail inside of PhpLiteAdmin that we can't use PhpLiteAdmin to fix the wrong path in the DB.
// and to make this software future compatible, I did not want to modify phpliteadmin.php at all
addDatabasesFromSqliteDb(true);

echo "<script>location.href='phpliteadmin.php';</script>";
?>
