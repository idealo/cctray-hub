# cctray hub

This service translates the github workflow API to the CCTray (cc.xml) format to allow seeing your build chain status on your desktop
in tools like [BuildNotify](https://anaynayak.github.io/buildnotify/) or [ccmenu](https://ccmenu.org/)

<img alt="buildnotify example" src="buildnotify.png">

## how to use

cctray-hub provides dynamic URLs based on three strings that you need to provide:

* repo owner
* repo name
* workflow name or id

```http://localhost:8080/cctray/<organisation-or-owner>/<repo>/<workfow-name-or-id>```

for cctray itself this will be:

```http://localhost:8080/cctray/idealo/cctray-hub/gradle.yml```


Note that cctray-hub will query builds from the main branch per default. 
If you need other (or even all) branches to get queried as well, feel free to open an issue.

## references

CCTray spec
* https://cctray.org/v1/

github docs regarding workflows
* https://docs.github.com/en/rest/reference/actions#get-a-workflow

## credits

cctray-hub is heavily inspired by this project: https://github.com/joejag/github-cctray

