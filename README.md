# Westworld Top 10 Words Analysis
I :heart: Westworld. This repository summarizes the work I've done to explore the top 10 words spoken by some of the major characters of Westworld.

Sources:

* Pretty SQL data joined and retrieved from: (Mode Analytics)[https://modeanalytics.com/modeanalytics/tables/westworld_episodes/?utm_medium=microsite&utm_source=westworlddata]
* Raw scripts from: (Springfield! Springfield!)[https://www.springfieldspringfield.co.uk/]
* Visualization adapted from: [Perkowitz's Westworld Personality Matrix](https://github.com/perkowitz/westworld-personality)

# 1. Requirements
Caveat: only tested on Mac, would love Windows contributions!

### 1a. Recommended Pre-requisities
* Python 3
* Virtual Environment

### 1b. Data Analysis
* Jupyter Notebook
* Pandas
* Numpy
* NLTK

### 1c. Data Visualization
* Maven
* Java JRE 8+

## 2. Westworld Personality Matrix

Build jar:

`mvn package && java -jar target/westworld-1.0.0-shaded.jar`

From the original docs:

``` To create a matrix from your personality data, send the CSV filename to Host. The `host.sh` script
provides a simple interface. Just run `host.sh example.csv`. In that example, your matrix will be created as `example.png`. ```
