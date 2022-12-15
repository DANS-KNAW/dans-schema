%define _tmppath %{_topdir}/BUILD
%define __jar_repack 0
Name: dans.knaw.nl-dans-schema-rpm
Version: 0.3.0
Release: SNAPSHOT20221215135652
Summary: DANS XML Schemas RPM Build
License: Apache 2.0
Vendor: dans.knaw.nl
Group: Applications/Archiving
Packager: dans.knaw.nl
autoprov: yes
autoreq: yes
BuildArch: noarch
BuildRoot: /home/janm/git/service/data-station/dans-schema/rpm/target/rpm/dans.knaw.nl-dans-schema-rpm/buildroot

%description
Defines the default configuration and version for plug-ins used in DANS projects.

%install

if [ -d $RPM_BUILD_ROOT ];
then
  mv /home/janm/git/service/data-station/dans-schema/rpm/target/rpm/dans.knaw.nl-dans-schema-rpm/tmp-buildroot/* $RPM_BUILD_ROOT
else
  mv /home/janm/git/service/data-station/dans-schema/rpm/target/rpm/dans.knaw.nl-dans-schema-rpm/tmp-buildroot $RPM_BUILD_ROOT
fi


%files
%defattr(744,root,root,755)
 "/var/www/html/schemas"

%prep

%pretrans

%pre

%post

%preun

%postun

%posttrans

%verifyscript

%clean
