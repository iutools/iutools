<?php

$request = $_POST['request'];
$expansions = array($request.'blah',$request.'bloh');
echo json_encode($expansions);