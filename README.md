# NutriMind
NutriMind is a project I made for school. It is a nutrition logging app that allows you to use your own AI image classifier model to detect food and look up nutrition info.
NutriMind is built on top of the [TM-ImageClassifer](https://github.com/THEWHITEBOY503/TM-ImageClassifier) framework. **In order for NutriMind's AI image classifier to work, you need to download or create an image classifier model, and set up the web server/classifier file. Instructions on how to do this are provided in the TM-ImageClassifier repository, as linked. Please read the `README.md` file before proceeding.**

## What does NutriMind do?
NutriMind is an experimental console nutrition logging app that uses AI image classification to look up a food item, then get nutrition info for it. 
As of present, NutriMind does NOT actually log nutrition info. (I know, sorry) As of me writing this, this project is due ~~**tomorrow**~~. I built a working demo, and as much as I would LOVE to make this a fully fleshed out app, I simply do not have the time. Perhaps that may change in the future if I ever get bored, so if you'd be intersted in seeing that, you should watch this repository, just in case I drop a surprise. Nonetheless, I'm uploading my code to this repository in hopes that if anyone wants to build a similar project, they can use this at least as a starting point. 
(Here's a tip for if you're using this for a school project and need help writing the food logging portion-- What you need is an SQL database, and when you get the nutrition data, log it to the SQL database along with the time and date. Then, when you open the log, have it display all entires where the date equals to today's date, then have a sum of all the nutrition values. You're welcome, good luck! :)) 

## Dependancies
This project (not the image classifier) was build with the Maven depenancies `jsch` and `json`.
The needed pom.xml file is included. Make sure you run `mvn install`, and hopefully the dependancies will install themselves. I don't know, I honestly don't understand Maven and Java dependancies very well, so 

## If you'd like to contribute, please feel free to make a pull request!
Even though this project is marked as "Completed", there's always room for improvement! I'd love to see and ideas or changes you may have!

## Attributions
This project was written with the assistance of ChatGPT, a large language model trained by [OpenAI](https://openai.com/). [(Their GitHub)](https://github.com/openai)

## Licensing
This project is licensed under the MIT license. It's free, but I'm sure you knew that, given that it's open-source and freely available on GitHub. If you'd like to put your own spin on this project, please feel absolutely free to! However, this software is provided "as-is", with no warranty of any kind, express or implied. For more information on the MIT license as it applies to this project, [Please see the licensing file here.](https://github.com/THEWHITEBOY503/NutriMind/blob/main/LICENSE) 
