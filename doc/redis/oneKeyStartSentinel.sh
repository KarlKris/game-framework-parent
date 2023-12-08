#! /bin/bash
echo start master
nohup src/redis-server redis.master.conf>/dev/null 2>&1&
echo start 2 slaves
nohup src/redis-server redis.slave6387.conf>/dev/null 2>&1&
nohup src/redis-server redis.slave6388.conf>dev/null 2>&1&
echo start 3 sentinels
nohup src/redis-sentinel sentinel26386.conf>/dev/null 2>&1&
nohup src/redis-sentinel sentinel26387.conf>/dev/null 2>&1&
nohup src/redis-sentinel sentinel26388.conf>/dev/null 2>&1&
echo success
