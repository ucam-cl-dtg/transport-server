sudo apt-get install tomcat6 tomcat6-admin libpg-java
sudo ln -s /usr/share/java/postgresql.jar /usr/share/tomcat6/lib/

sudo ufw allow proto tcp from any to any port 8443
sudo /etc/init.d/tomcat6 start
sudo apt-get install postgresql-8.4 postgresql-8.4-postgis 

cl-asuser mkfs -s 1 -b 4096 -i 16000 -j -L minibusdata /dev/xvdb
echo "LABEL=minibusdata /data auto defaults 1 2" | sudo tee --append /etc/fstab
sudo mkdir /data
sudo mount /data
sudo mkdir /data/postgres
sudo chown postgres:postgres /data/postgres/
echo | sudo su postgres -c "psql -d template1" <<EOF
create tablespace data location '/data/postgres';
#create database minibus with encoding='UTF-8' tablespace data;
create user minibus with password 'minibus';
grant all privileges on database minibus to minibus;
EOF
sh /usr/share/postgresql-8.4-postgis/utils/postgis_restore.pl /usr/share/postgresql/8.4/contrib/postgis.sql minibus /tmp/minibus.sql > /tmp/restore.log

Add 
<Resource auth="Container" driverClassName="org.postgresql.Driver"
 maxActive="8" maxIdle="4" name="jdbc/generaldb" password="minibus"
 type="javax.sql.DataSource" url="jdbc:postgresql://localhost/minibus"
 username="minibus"/>
To /etc/tomcat6/context.xml


# deploy webapp
mvn package tomcat6:deploy
