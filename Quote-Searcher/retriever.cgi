#!/bin/bash
echo "Content-type: text/html"
echo ""

echo '<html>'
echo '<head>'
echo '<title>Quote Crawler : a search engine for quotations</title>'
echo '<link rel="stylesheet" type="text/css" href="style.css" media="screen" />'
echo '</head>'
echo '<body>'

echo "<h1 id='pagetitle'>QuoteCrawler</h1>"
echo "<span id='authorname'>(by Yijie Feng) </span>"
echo '<span class="citationlink"> &nbsp;|&nbsp; <a href="cited.html">Credits and Citations</a></span>'

echo '<br/><p class="instructions">Accepts input for a Query (required), Speaker (optional), Lower Bound Sentiment Probability (optional), and Upper Bound Sentiment Probability (optiona)</p>'
echo '<p class="instructions">The search engine searches for quotes relevant to your query. From those results, it will filter by speaker (if one is provided). The uppder and lower bound sentiment probabilities filter out quotes that fall out of the specified range. The probability represents the probability that the quote has a positive sentiment -- if you want to search for negative quotes, you could use the range 0.0 - 0.5. All fields are required.</p>'

echo "<form method=GET action=\"${SCRIPT}\">"
    
echo '<br/> <span class="formlabel">query string (or phrase):</span><input id="queryinput" type="text" name="val_q" size=20>'

echo '<br/> <span class="formlabel">speaker filter:</span> <input id="speakerinput" type="text" name="val_s" value="all" size=20>'
echo '<br/> <span class="formlabel">sentiment probability (lower bound):</span> <input id="xinput" type="text" name="val_x" value="0.0" size=20>'
echo '<br/> <span class="formlabel">sentiment probability (upper bound):</span> <input id="yinput" type="text" name="val_y" value="1.0" size=20>'

echo '<br/> <input class="button" type="submit" value="Search">'
echo '<input class="button" type="reset" value="Reset">'
echo '</form>'

echo '<br/><br/><b>Examples:</b>'
echo '<p><em> query: politics, speaker: clinton </em></p>'
echo '<p><em> query: money, speaker: all </em></p>'
echo '<p><em> query: sequels, speaker: Schwarzenegger </em></p>'
echo '<p><em> query: oil, speaker: all, sentiment lower bound: 0.0, sentiment upper bound: 0.5 </em></p>'
echo '<p><em> query: economy, speaker: all </em></p>'
echo '<p><em> query: military, speaker: all </em></p>'



    if [ -z "$QUERY_STRING" ]; then
	#echo "NO QUERY STRING PROVIDED"
	exit 0
    
    else
	#echo "QUERY PROVIDED  "
	
	query=`echo "$QUERY_STRING" | sed -n 's/^.*val_q=\([^&]*\).*$/\1/p' | sed "s/%20/ /g\
#{}"`

	speaker=`echo "$QUERY_STRING" | sed -n 's/^.*val_s=\([^&]*\).*$/\1/p' | sed "s/%20/ /g\
#{}"`
	lowersent=`echo "$QUERY_STRING" | sed -n 's/^.*val_x=\([^&]*\).*$/\1/p' | sed "s/%20/ /g\
#{}"`
	uppersent=`echo "$QUERY_STRING" | sed -n 's/^.*val_y=\([^&]*\).*$/\1/p' | sed "s/%20/ /g\
#{}"`
 
     echo "<h3><a href='results.html'> >> RESULTS PAGE </a></h3> <br/><br/>"
     


	 java -cp .:./jars/* Searcher -index ./_index/ -q $query -s $speaker -x $lowersent -y $uppersent

	 echo "<script> window.location='http://cims.nyu.edu/~yf833/cgi-bin/results.html'; </script>>"

  fi


echo '</body>'
echo '</html>'

exit 0
