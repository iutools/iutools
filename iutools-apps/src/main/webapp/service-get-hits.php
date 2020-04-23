<?php
$query = $_POST['query'];
$hits = array(
    array(
      'url' => 'http://somewhere.com/hello.html',
      'title' => 'Welcome to Hello!',
      'snippet' => 'This site is about <em>hello</em> ... contains information about <em>hello</em> ...',
      'content' => 'Blah blah blah...'
    ),
    array(
      'url' => 'http://somewhere.com/hello2.html',
      'title' => 'Welcome to Hello2!',
      'snippet' => 'This site is about <em>hello</em> ... contains information about <em>hello</em> ...',
      'content' => 'Blah2 blah2 blah2...'
    ),
    array(
      'url' => 'http://somewhere.com/hello3.html',
      'title' => 'ᐅᓪᓗᖃᑦᓯᐊᕆᑦ',
      'snippet' => 'blah blah blah <em>ᐅᓪᓗᖃᑦᓯᐊᕆᑦ</em> ... contains information about <em>ᐅᓪᓗᑦᓯᐊᖅ</em> ...',
      'content' => 'Blah2 blah2 blah2...'
    )
    );  
echo json_encode($hits);    
