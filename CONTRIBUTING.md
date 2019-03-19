# How to start contributing?

## Familiarity
* Familiarize yourself with the [Android libraries](https://developer.android.com/training/connect-devices-wirelessly)
used in this project: Network Service Discovery (LAN),
[WiFi Direct](https://developer.android.com/training/connect-devices-wirelessly/wifi-direct) and
[Bluetooth](https://developer.android.com/guide/topics/connectivity/bluetooth).

## Fork the Repository

1. go to [https://github.com/sabzo/ayanda](https://github.com/sabzo/ayanda)
2. hit the "fork" button and choose your own github account as the target
3. for more detail see [http://help.github.com/fork-a-repo/](http://help.github.com/fork-a-repo/)

## Setup your Local Development Environment

1. `git clone git@github.com:<your-github-username>/ayanda.git`
2. `cd ayanda`
3. `git remote show`  
_you should see only 'origin' - which is the fork you created for your own github account_
4. `git remote add upstream git@github.com:sabzo/ayanda.git`
5. `git remote show`  
_you should now see 'upstream' in addition to 'origin' where 'upstream' is the spring-projects repository from which releases are built_
6. `git fetch --all`
7. `git branch -a`
_you should see branches on origin as well as upstream, including 'master'_

## Keeping your Local Code in Sync

* As mentioned above, you should always work on topic branches (since 'master' is a moving target). However, you do want to always keep your own 'origin' master branch in sync with the 'upstream' master.
* Within your local working directory, you can sync up all remotes' branches with: `git fetch --all`
* While on your own local master branch: `git pull upstream master` (which is the equivalent of fetching upstream/master and merging that into the branch you are in currently)
* Now that you're in sync, switch to the topic branch where you plan to work, e.g.: `git checkout -b example_branch`
* When you get to a stopping point: `git commit`
* If changes have occurred on the upstream/master while you were working you can sync again:
    - Switch back to master: `git checkout master`
    - Then: `git pull upstream master`
    - Switch back to the topic branch: `git checkout example_branch` (no -b needed since the branch already exists)
    - Rebase the topic branch to minimize the distance between it and your recently synced master branch: `git rebase master`
* **Note** that you can always force push (git push -f) reworked / rebased commits against the branch used to submit your pull request. In other words, you do not need to issue a new pull request when asked to make changes.
* Now, if you issue a pull request, it is much more likely to be merged without conflicts. Most likely, any pull request that would produce conflicts will be deferred until the issuer of that pull request makes these adjustments.
* Assuming your pull request is merged into the 'upstream' master, you will actually end up pulling that change into your own master eventually, and at that time, you may decide to delete the topic branch from your local repository and your fork (origin) if you pushed it there.
    - to delete the local branch: `git branch -d example_branch`
    - to delete the branch from your origin: `git push origin :example_branch`
    
    
### Pull Requests
Pull requests are the main part of contributing to open source.Verify that there are no conflicts of your forked repo with the base repo and make a pull request adding the description of the feature you have added and wait for the maintainer to merge the request for you.If the pull request satisfies all the properties it is then merged with the base repo and 
`CONGRATULATIONS` now you have become a member of contributors to the repo.

#### Pull Request Process
 1. Always work on your own fork of the repository and open pull requests when necessary.
 2. Ensure any install or build dependencies are removed before the end of the layer when doing a build.
 3. When working on a bug/issue , open a pull request as soon as you start working and mark
 it as `[WIP]` (Work in Progress), so that the developers can keep an eye on the work going on.
 4. Update the README.md with details of changes to the interface, this includes new environment variables, new classes, algorithms, useful file locations and container parameters.
 5. You may merge the Pull Request in once you have the sign-off of the maintainers, or if you do not have permission to do that, you may request the maintainer to do it for you.

#### Communication
To report a problem view the existing [issues](https://github.com/sabzo/ayanda/issues) 
or create a new issue if it isn't already there.

#### Testing
The goal of testing is to ensure that multiple Android devices can discover each other and communicate
with each other by sharing files between each other. Discovering and Sending can be achieved through
Bluetooth, Network Service Discovery (Local Area Network), Wifi Direct.

* two or more android devices (or an Android emulator that has bluetooth, gps and wifi functionality).
This project includes a sample test app that allows file sharing by Bluetooth, LAN, and WifiDirect.

Try sharing files using all three methods -- note the successes and failures. If you experience errors
run the app in debug mode and note the errors in an existing issue (or a new one if the issue isn't noted).
