# Social Media Toolkit
I wrote this code and quite a few other utilities between 2011 and 2012 when I was working on my master's thesis, but since much of the code is now defuct I'm just saving the most interesting bits here for posterity. 

## Overview
This repository contains a haphazard collection of code and utilities I wrote during my master's thesis. The two primary projects are the `Facebook Crawler` project which crawls Facebook (outside the restricted Facebook Graph API) and a set of graph utilities in the `Social Media Toolkit` utilities project for large scale graph isomomorpism/deanonymization on Neo4J graph databases. The deanonymization routines were based off of the following inspirational works.

- [Arvind Narayanan, Elaine Shi, and Benjamin I. P. Rubinstein. Link prediction by de- anonymization: How we won the kaggle social network challenge. CoRR, abs/1102.4374, 2011.](https://arxiv.org/abs/1102.4374)
- [Arvind Narayanan and Vitaly Shmatikov. De-anonymizing social networks. Technical report, University of Texas, Austin.](https://www.cs.utexas.edu/~shmat/shmat_oak09.pdf)
- [Arvind Narayanan and Vitaly Shmatikov. Robust de-anonymization of large sparse datasets. Technical report, University of Texas, Austin.](https://www.cs.cornell.edu/~shmat/shmat_oak08netflix.pdf)

## Research
My thesis which I now find embarrassing to read is available at [http://lib.dr.iastate.edu/etd/12347/](http://lib.dr.iastate.edu/etd/12347/). Additional papers can be found at [ben-holland.com/publications](https://ben-holland.com/publications).

## Disclaimer
Parts of this code almost certaintly violate Facebook's terms of service or are likely no longer functional. Please use this code for educational purposes only and even in doing so, you may need to consult your universities IRB committee to design a legal and ethical experiment involving deanonymization of social private network data.
