FROM osixia/openldap:1.1.7

ADD anonymous-bind.ldif /container/service/slapd/assets/anonymous-bind.ldif
ADD data-people.ldif /container/service/slapd/assets/data-people.ldif
ADD data-roles.ldif /container/service/slapd/assets/data-roles.ldif

ADD certs/ca.crt /container/service/slapd/assets/certs/ca.crt
ADD certs/ldap.crt /container/service/slapd/assets/certs/ldap.crt
ADD certs/ldap.key /container/service/slapd/assets/certs/ldap.key

ENV LDAP_TLS_VERIFY_CLIENT allow
ENV LDAP_TLS_CA_CRT_FILENAME ca.crt
ENV LDAP_TLS_CRT_FILENAME ldap.crt
ENV LDAP_TLS_KEY_FILENAME ldap.key