# Gitlet Design Document
author: Frank Jin

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures
### Main: the driver for the project, will take in commands and find the correct methods to execute.
#### Class Variables
* File _repositoryDir: gets the current user directory. Assums it is called form the main
repository folder
* boolean _initialized: Checks if this is an active repository, by checking is a 
.gitlet folder exists
* CommitTree _repo: The commitTree file

### Commit: Is a serializable object, which represent the commits of gitlet.
#### Instance Variables
* TreeMap<String, String> blobList: Lists of file names and hashcodes of all blobs that this commit references.
* String prevCommitID: Hashcode of previous commit's ID.
* String timeOfCommit: Time of commit, formatted for log.

### Blobs: This object contains the methods that turn objects into blobs

### Staging
#### Class Variables
* Remove List: A list of files to remove/not to include in next commit
* Add: A list of files to be added in next commit

### CommitTree: Keeps track of all the commits, and contains most of the programs functions
#### Instance Variables
* String _head: Hash of the current head of the repository.
* TreeMap<String, String> _branches: A treemap of all the branches that were created, and the commit hash
that they point to currently.
* String _currentBranch: The name of the current branch
* File _commitDump: File location for the commitDump.
* File _blobDump: File location for the blobDump.
* File _addStaging: File location for adding files to commits.
* File _rmvStaging: File location for removing files from commits.
* TreeMap<String, String> _commitMessages: Dictionary of commit hash and commit message.

## 2. Algorithms
### Main
#### Functions
* Main: Takes in user commands and commandSwitch to select the right ones to execute. 
Initializes the repository at the beginning.
* CommandSwitch: Contains a series of checks to make sure syntax is valid to execute a command. Exits with code 0.

### Commit
#### Functions
* Constructor: The constructor will take in the commit message, time, blobs, previous commit 
  to create a new commit instance. Also creates a new commit log file.
* void createBlobList(): Looks through the current commit files, and sees if some already exist 
  in the blob directory. If some do, pointer to them will be created. For
  new blobs, they will be created and added to the blob directory.
* String getTime(): returns time of commit.
  
### Blob
#### Functions
* Getters: Getters for the class
* Constructor: sets the instance variables for the class

### Staging
#### Functions
* Rem: Put a file in the remove list if not staged, if staged then remove form add list
* Add: Put file in the add list

### Tree
#### Functions
* Constructor: Initializes the repository, creates file structure and makes the first
  commit.
* add(String filename): Blobify a file in the working directory and adds it to the add-staging
  folder.
* save(Commit currentCommit, String commitMessage): Takes a commit and commit message and write it into the commitDump folder
  with its name being the SHA1 code for it. Updates _head to point to this
  commit, and adds the code and message to the commitCodes dictionary.
* commit(String commitMessage, Boolean initialCommit): Refers to the staging class to see what files needs to be commited, and then
  calls the commit class to commit those files. Adds to the commit log file.
* log(): Prints out the commit log file.
* global log: Prints the log of all the commit files in the commit folder
* find: starting from the master pointer, looks backwards to find commits with matching commit
  messages
* status: prints the status of the repo by refering to staging class.
* checkoutFile (String filename): Opens the latest commit on the current branch and opens the blob that
  filename corresponds to. Then deletes the file in the working directory,
  and writes in the one that was found.
* checkoutFile (String commitID, String filename): Same thing as prevoud checkoutFile() function, but this time a commitID
  is specified.
* Branch: TBD
* rmBranch: TBD
* Merge: TBD
* Reset: TBD

## 3. Persistence
The following files will be in .gitlet
* CommitTreeFile: CommitTree object file.
* blobDump: Folder will all the blob files.
* commitDump: Folder with all the commit history files.
* addStaging: Folder for all the added files.
* rmvStaging: Folder for all the removed files.


