# Demo Project to reproduce spring ldap issue #430

Issue: https://github.com/spring-projects/spring-ldap/issues/430
Merge Request: https://github.com/spring-projects/spring-ldap/pull/432

Big Thanks to https://github.com/zivis

## What causes the bug

* Groupsearch is used
* Anonymous bind is allowed and can search through the directory tree
* `DefaultTlsDirContextAuthenticationStrategy` is used for starttls


## With DefaultTlsDirContextAuthenticationStrategy

Start a TLS connection and a lookup if the user `cn=user` is available.

```bash
588241ed conn=1011 fd=16 ACCEPT from IP=172.17.0.1:44150 (IP=0.0.0.0:389)
588241ed conn=1011 op=0 EXT oid=1.3.6.1.4.1.1466.20037
588241ed conn=1011 op=0 STARTTLS
588241ed conn=1011 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
588241ed conn=1011 fd=16 TLS established tls_ssf=128 ssf=128
588241ed conn=1011 op=1 BIND dn="" method=128
588241ed conn=1011 op=1 RESULT tag=97 err=0 text=
588241ed conn=1011 op=2 SRCH base="ou=People,dc=example,dc=org" scope=2 deref=3 filter="(cn=user)"
588241ed <= bdb_equality_candidates: (cn) not indexed
588241ed conn=1011 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
588241ee conn=1011 op=3 UNBIND
588241ee conn=1011 fd=16 closed
```

Empty dn="" in `588241ee conn=1013 op=1 BIND dn="" method=128` forces an anonymous bind and allows the user the access with any password given

```bash
588241ee conn=1012 fd=16 ACCEPT from IP=172.17.0.1:44158 (IP=0.0.0.0:389)
588241ee conn=1012 op=0 EXT oid=1.3.6.1.4.1.1466.20037
588241ee conn=1012 op=0 STARTTLS
588241ee conn=1012 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
588241ee conn=1012 fd=16 TLS established tls_ssf=128 ssf=128
588241ee conn=1012 fd=16 closed (connection lost)
588241ee conn=1013 fd=16 ACCEPT from IP=172.17.0.1:44162 (IP=0.0.0.0:389)
588241ee conn=1013 op=0 EXT oid=1.3.6.1.4.1.1466.20037
588241ee conn=1013 op=0 STARTTLS
588241ee conn=1013 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
588241ee conn=1013 fd=16 TLS established tls_ssf=128 ssf=128
588241ee conn=1013 op=1 BIND dn="" method=128
588241ee conn=1013 op=1 RESULT tag=97 err=0 text=
588241ee conn=1013 op=2 SRCH base="ou=Groups,dc=example,dc=org" scope=1 deref=3 filter="(member=cn=user,ou=people,dc=example,dc=org)"
588241ee conn=1013 op=2 SRCH attr=cn objectClass javaSerializedData javaClassName javaFactory javaCodeBase javaReferenceAddress javaClassNames javaRemoteLocation
588241ee <= bdb_equality_candidates: (member) not indexed
588241ee conn=1013 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
588241ee conn=1013 op=3 UNBIND
588241ee conn=1013 fd=16 closed
```

## With FixDefaultTlsDirContextAuthenticationStrategy

Start a TLS connection and a lookup if the user `cn=user` is available.

```bash
58824372 conn=1014 fd=16 ACCEPT from IP=172.17.0.1:44276 (IP=0.0.0.0:389)
58824372 conn=1014 op=0 EXT oid=1.3.6.1.4.1.1466.20037
58824372 conn=1014 op=0 STARTTLS
58824372 conn=1014 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
58824373 conn=1014 fd=16 TLS established tls_ssf=128 ssf=128
58824373 conn=1014 op=1 BIND dn="" method=128
58824373 conn=1014 op=1 RESULT tag=97 err=0 text=
58824373 conn=1014 op=2 SRCH base="ou=People,dc=example,dc=org" scope=2 deref=3 filter="(cn=user)"
58824373 <= bdb_equality_candidates: (cn) not indexed
58824373 conn=1014 op=2 SEARCH RESULT tag=101 err=0 nentries=1 text=
58824373 conn=1014 op=3 UNBIND
58824373 conn=1014 fd=16 closed
```

Here happens a bind with user credentials in `58824373 conn=1015 op=1 BIND dn="cn=user,ou=People,dc=example,dc=org" method=128` which is the actual authentication and the user with wrong credentials can not log in

```bash
58824373 conn=1015 fd=16 ACCEPT from IP=172.17.0.1:44280 (IP=0.0.0.0:389)
58824373 conn=1015 op=0 EXT oid=1.3.6.1.4.1.1466.20037
58824373 conn=1015 op=0 STARTTLS
58824373 conn=1015 op=0 RESULT oid= err=0 text=
TLS: gnutls_certificate_verify_peers2 failed -49
58824373 conn=1015 fd=16 TLS established tls_ssf=128 ssf=128
58824373 conn=1015 op=1 BIND dn="cn=user,ou=People,dc=example,dc=org" method=128
58824373 conn=1015 op=1 RESULT tag=97 err=49 text=
58824373 conn=1015 fd=16 closed (connection lost)
```


## To Reproduce

1. Import the ssl ca certificate used within the ldap docker container to your java keystore  

```bash
sudo keytool -import -alias caopenldapdocker -file docker/certs/ca.crt -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -trustcacerts
```

2. Go into the `docker/` directory and build the docker image with the name `openldap`

```bash
docker build -t openldap .
```

3. Start the docker container with the hostname `ldap.example.org` and add `127.0.0.2 ldap.example.org` into your `/etc/hosts`

```bash
docker run --hostname ldap.example.org --rm --name my-openldap-container -p 389:389 openldap
```


4. Import the provided `anonymous-bind.ldif` into ldap

```bash
docker exec my-openldap-container ldapadd -Y EXTERNAL -H ldapi:/// -f /container/service/slapd/assets/anonymous-bind.ldif
```

5. Import the provided `data-roles.ldif` into ldap 

```bash
docker exec my-openldap-container ldapadd -x -D "cn=admin,dc=example,dc=org" -w admin -f /container/service/slapd/assets/data-roles.ldif -h ldap.example.org -ZZ
```

6. Import the provided `data-people.ldif` into ldap. This will provide a user with `user/user` to login into the application

```bash
docker exec my-openldap-container ldapadd -x -D "cn=admin,dc=example,dc=org" -w admin -f /container/service/slapd/assets/data-people.ldif -h ldap.example.org -ZZ
```

7. Use the `DefaultTlsDirContextAuthenticationStrategy` to login with the username `user` and every password you want to use in the `src/main/java/org/tobsch/AuthenticationManagerConfiguration.java`. It is activated by default in the demo project.


8. If you want to check the solution provided in https://github.com/spring-projects/spring-ldap/pull/432 just use the `FixDefaultTlsDirContextAuthenticationStrategy` in `AuthenticationManagerConfiguration`.



##### Additional

You can query the ldap database with

```bash
docker exec my-openldap-container ldapsearch -x -h localhost -b dc=example,dc=org -D "cn=admin,dc=example,dc=org" -w admin
```
