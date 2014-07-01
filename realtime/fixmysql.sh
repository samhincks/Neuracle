mysqld stop
touch /tmp/mysql.sock
mysqld_safe restart

mysql.server start
