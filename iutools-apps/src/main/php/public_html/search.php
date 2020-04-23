<?php
session_start();
$request = null;
$hits = null;
$page = 1;
$expansions = null;

$nbHitsDisplayedPerPage = 2;
$hitsDisplayed = null;
$index = null;
$nbPages = null;
$newRequest = null;

//foreach ($_POST as $key => $value)
//    echo '$_POST[\''.$key.'\']'.' = '.$value.'<br>';

if (isset($_POST['submit'])) {
    $newRequest = true;
    $_SESSION['hits'] = null;
}
if (isset($_POST['request']))
    $request = $_POST['request'];
if (isset($_POST['page-number']))
    $page = $_POST['page-number' ];

if (isset($_SESSION['hits']))
    $hits = $_SESSION['hits' ];

if ( isset($request) ) {
    if ( !isset($hits) ) {
        $expansions = join(' ',getExpansions($request));
        $expandedQuery = $request.' '.$expansions;
        $hits = getHits($expandedQuery);
        if ($hits===FALSE) {
        } else {
            $_SESSION['hits'] = $hits;
        }
    } else {
        $expandedQuery = $request;
    }
    $nbHits = count($hits);
    $nbPages = floor($nbHits / $nbHitsDisplayedPerPage);
    if ($nbHits % $nbHitsDisplayedPerPage != 0)
        $nbPages++;
    $index = ($page-1)*$nbHitsDisplayedPerPage;
    $hitsDisplayed = array_slice($hits,$index,$nbHitsDisplayedPerPage);
    if (isset($_POST['previous-page']) && $page>1)
        $page--;
    elseif (isset($_POST['next-page']) && $page<$nbPages-1)
        $page++;
}
// ----------------------- Functions ---------------------------
function getExpansions($request) {
    $url = "http://localpirurvik:8888/service-get-expansions.php";
    $data = array('request' => $request);
    // use key 'http' even if you send the request to https://...
    $options = array(
        'http' => array(
            'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
            'method'  => 'POST',
            'content' => http_build_query($data)
        ));
    $context  = stream_context_create($options);
    $result = file_get_contents($url, false, $context);
    if ($result === FALSE) { 
        /* Handle error */ 
        $expansions = FALSE;
    } else {
        $expansions = json_decode($result,true);
    }
    return $expansions;    
}

function getHits($expandedQuery) {
    $url = "http://localpirurvik:8888/service-get-hits.php";
    $data = array('query' => $expandedQuery);
    // use key 'http' even if you send the request to https://...
    $options = array(
        'http' => array(
            'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
            'method'  => 'POST',
            'content' => http_build_query($data)
        ));
    $context  = stream_context_create($options);
    $result = file_get_contents($url, false, $context);
    if ($result === FALSE) { 
        /* Handle error */ 
        $hits = FALSE;
    } else {
        $hits = json_decode($result,true);
    }
    return $hits;    
}?>

<!doctype html>

<html lang="en">
<head>
  <meta charset="utf-8">

  <title>Pirurvik</title>
  <meta name="description" content="Pirurvik">
  <meta name="author" content="Benoît Farley">

  <link rel="stylesheet" href="css/styles.css?<?php echo time();?>">

</head>

<body>
    <h1>NRC's Inuttut Search Engine</h1>
    
    <form id="form-query" method="post" action="">
    Enter your request:
    <input id="request" name="request" type="text" value="<?php echo $expandedQuery;?>" />
    <button id="submit-request" type="submit" name="submit" value="submit">Submit</button>
    
    <p></p>
    
        <?php if (isset($hitsDisplayed)) { ?>
    <div id="links-to-pages" style="display:block;">
      <div id="links">
        <button id="previous-page" 
            type="submit" <?php if ($page==1) { ?>disabled<?php } ?>
            name="page-number" value="<?php echo $page==1? '1' : $page-1; ?>">Previous</button>
        <?php for($i=0; $i<$nbPages; $i++) { ?>
        <input class="page-number<?php if (($i+1)==$page) echo ' current-page';?>"
            type="submit"
            name="page-number" value="<?php echo $i+1;?>"/>
        <?php } ?>
        <button id="next-page" 
            type="submit" <?php if ($page==$nbPages) { ?>disabled<?php } ?>
            name="page-number" value="<?php echo $page==$nbPages? $page : $page+1;?>">Next</button>
       </div>
      </div>
        <?php } ?>
    </form>

    <?php if (isset($hitsDisplayed)) { ?>
    <div id="hits" style="display:block;">
      <div id="nb-hits"><?php echo $nbHits;?> result<?php if ($nbHits>1) echo 's';?></div><hr>
      <?php foreach ($hitsDisplayed as $aHit) { ?>
      <div class="hit">
        <div class="hit-disp title">
            <a class="hit-title" href="<?php echo $aHit['url'];?>"><?php echo $aHit['title'];?></a>
            </div>
        <div class="hit-disp">
            <a class="hit-url" href="<?php echo $aHit['url'];?>"><?php echo $aHit['url'];?></a>
            </div>
        <div class="hit-disp">
            <span class="hit-snippet"><?php echo $aHit['snippet'];?></span>
            </div>
        </div>
      <?php } ?>
    </div>
    <?php } ?>
    
</body>

</html>