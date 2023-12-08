$pid=`ps aux |grep java |grep -v grep |awk '{print $2}'`
kill $pid