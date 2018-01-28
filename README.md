# Halite2Bot

A link to the document with better formatting:

https://docs.google.com/document/d/1BuP3hApHRqSpKVV33-Ti41BR7cEngdlan9tzvEpBaNY/edit?usp=sharing

Feel free to add or change anything there.

**TL;DR**

Thank you to the development team and everyone on the discord. I had lots of fun. Collision time is really important and rushing is the devil incarnate. For the Halite Team: There is some feedback in this document. See: Feedback. It is coloured red for you to easily find it.

**Acknowledgements**

I first signed up for Halite II in December of 2017, and started coding my bot on December 29th. Around that time was when I first submitted my first bot on the account ClearMonocle. From December 29th to January 7th, I worked on my bot everyday. There wasn’t a moment where I did not think about my bot and I truly loved this experience. This was my first coding competition and I will definitely return for Halite III. It has been an extremely long time anything has made me as excited and passionate for anything as Halite II. This is all thanks to the Halite Team and I would love to thank them for this wonderful experience that they have provided me. Along with everyone in the Discord server that also contained some truly awesome people. I would like to especially acknowledge mlomb for helping a whole bunch for my bot and even teaching me a lot of new skills. People like dhallstr, birsolunut, zer0, x052 and countless others accompanied me on this incredible journey. Thank you for helping and supporting me in your own ways throughout all Halite II. It truly feels great being in a thriving community full of passionate and kind people competing directly against you, yet willing to assist you when you need it. Thank you to Lunariz for sustaining the Discord server that houses this wondrous community.

**Introduction**

This document outlines the strategies I used, how my bot works and what I could have improved on. I hope you find this both entertaining to read and insightful. All of these larger sections will have subsections that delve into specifics. In case you haven’t realised, my bot has now been open sourced and can be accessed on this website. The bot that was submitted to this website was Guardian-v2.zip. Feel free to message me on the Halite discord server and ask me questions! Here is the link: https://discord.gg/kPvAWgx

**Strategies**

During the segment of time I was competing in Halite II, I saw several different popular strategies. Fohristiwhirl has written extensively about rushing here: https://github.com/fohristiwhirl/halite2_rush_theory. He has explained how to defend against rushes pretty well, and since I never had a rush defense mechanism, you should go to his explanation for how to defend against rushes. Rushing, coward, and rabbit were some of the most popular strategies I saw. It is important to note that strategies are not as important as mechanics. Focusing on your navigation and priorities is way more important than strategies.

**Rushing**

Rushing is what is referred to as not docking at all and attacking as soon as possible. This strategy is an all in, and if it doesn’t work, your rush fails and you lose. What I found is that disabling rush actually raised my rank slightly and in some bots the rank did not change it at all. I had rushing enabled up until I was a gold player. It is to your advantage to not rush. This is an example of the prisoner's dilemma, where both bots will benefit if they don’t rush, while both people rushing will ruin both players. If one bot rushes and the other does not, the rusher will gain the advantage, unless the non-rusher has a great rush defense like fohristiwhirl. Since I didn’t have a way of defending against rushes, it was entirely up to chance, and I most likely lost. Players mostly agree that two player rushing is not as frustrating as four player rushing, because four player rushes delays the rusher and you from docking while the other two players will dock and expand. This ends up in a loss for both the rusher and the victim in many cases. If the rusher is lucky, he will completely eliminate the victim from the game and take the planets that would have been taken by the victim. Rushing is scorned by those who do not rush and those who do rush feel a great guilt. I wonder how the people who rush sleep at night, knowing that they lowered the mu of an innocent player, especially the four player rushers. Rushing is the root of all evil on this earth.

**Coward**

	Coward is what is referred to as running away and hiding in corners when you know the game has been lost. This is an extremely helpful strategy, because running away means that you raise your chance of getting second place instead of third or fourth place. This might mean your mu will increase or that you will not lose as much mu. Some players call this strategy “stupid”, but I do partake in this “stupid” meta, mostly because it does increase my rank quite a bit. This does not harm other players unlike rushing. It does not affect other players in any way and sometimes helps them. It frees up planets for players to take. I recommend this strategy strongly, considering its ease of implementation.

**Rabbit**

Rabbit refers to sending one ship straight away to the enemy planet to harass. This strategy is a bit hard to implement and has a few prerequisites. To have this you should have either an advanced navigation system or an extremely heavy modification on the default starter kit navigation. You should also have code for retreating. I will talk about retreating later in this document. A brief description of retreating is to thrust in the opposite direction of the enemy. When the harassing ship has reached the opponent planet, it will either attack the docked ships, but more likely the docked ships would have manifested a new ship in the time that it took your ship to reach theirs. This harassing ship will then run away from the new ship but circle back to the docked ships to attempt to harass, but this will most likely cause newly produced ships to chase this harassing ship and instead of focusing on capturing planets these newly produced ships will be “defending” against this rabbit. While all this is happening on the opponent’s side, you should be settling planets and creating more ships to prepare the overwhelming attack on the opponent. I actually rescind my previous statement on rushing. While rushing is more common, this strategy infuriates me more than anything else. I have tried my hand in using the rabbit strategy but it never works and I just want to smash my keyboard against the table. Rabbits are the root of all evil on this earth.

**How My Bot Works**

This section deals with how I coded my bot and all the bits and pieces that make it work. This may be the most simple section yet my code is horribly convoluted and unreadable. Hopefully future employers will not look at this code.

**Navigation**

Through my experience on Halite II, I have come to learn that Navigation is the most important part of a bot. There is nothing else more significant. The first big step I took to improving my navigation was implementing collision time from the Halite engine. This boosted me from a previous rank 200 to a rank 60. I use collision time by check every angle starting from the desired angle, then I turn a certain degrees clockwise, then return to the desired angle and turn a certain degrees counterclockwise. After every rotation I check if I will collide with friendly ships which requires the use of collision time. I also use the default objectsBetween(start,target) to detect collisions with static objects. I keep a Map (, the java version of a dictionary in python,) of all ship vectors so I can know where my friendly ships will be so I don’t collide with them. I modified addEntitiesBetween(start,target) which is used by objectsBetween to only consider ships that are in the Map of all vectors because collision time will take care of moving ships. There is a major improvement I can make to my navigation. See What I Could Have Improved On. An earlier version of my bot clumped on to planets and remained still, unable to travel to their target. A fix for that I did was almost like increasing the size of planets. I headed towards a spot that was a certain distance away from the surface of the planet and I used that and the planet’s orientation towards the ship to calculate a new position to thrust to using trigonometry. This worked for a while but I got rid of it because the default navigation combined with collision time works fine to prevent this clumping. It still remains a solution for basic bots.

**Combat**

Combat was not as complicated for me as I thought it would be. My combat is as a result extremely rudimentary. My ship has an aggro range, which means it attacks enemies within a certain range. This range is set to 35 units and can be modified on the Constants.java file. It does not prioritise docked ships or vulnerable ships with low health. It simply attacks the nearest ships if they are within aggro range.

**Retreating**

Retreating served to raise my rank quite a bit. It was an important part of my bot because my ships would not just blindly attack the enemy even if it wasn’t wise to do so. At the beginning of my for loop (that iterates through every existing friendly ship,) I would determine if the ship was safe. Safe is defined as any ship 13 units away. This constant can also be modified in Constants.java. The reasoning for 13 units was that a ship radius is 0.5, therefore 1 in diameter added to the maximum travelling distance which is 7 added to the weapon ranges which is 5.  If the ship was in danger however, meaning if the amount of enemy ships within 20- an arbitrary number that I found -worked well- units outnumbered the amount of friendly ships within 7 units, then thrust towards the nearest safe ship. I also added a script where if there are no safe ships nearby, then thrust to the nearest ship and if there were no near ships at all, then simply thrust away. This script was included to promote clustering and it did have that effect, at the cost of some timeouts due to overclustering which resulted in a bit of lack of mobility.

**Prioritising**

Priority was also a significant factor in ranking. A change in priorities made my rank vary greatly. The priorities I have now are as follows:

Retreat

Coward

Attack

Dock

Move To Dock

I placed these priorities to ensure that my ships live, which to me is the most important part. You will notice that only the second last one would render a ship vulnerable.

**Difficulties**

This section will deal with some of the most difficult things I had to deal with beyond my knowledge and control.

**Collision Time**

I was able to add collision time a long time ago, but not implement it properly. My problem was rounding. Originally the starter kit used its own way to round that was missing the actual value by a small but significant amount. In the end, mlomb came up with a solution that was to convert the initial value to degrees, then back to radians. This helped immensely and completely fixed my problem. I was able to effectively get rid of all self-collisions. 



**Timeouts**

Timing out was not an extremely frequent problem, but a problem nonetheless. I find that I usually timeout at around 250 ships. I have measures to prevent this, but they don’t work as well as I would like them to. At the start of each ship’s iteration, I check if the time has exceeded 1900 milliseconds. If it has, I immediately terminate the loop and send the moves to STDOUT. To ease workload on each ship, I only get collision time to consider ships within 28 units of itself. This means that it won’t have to consider all ships, lessening the load considerably. Rewriting the code also helped a lot but I inevitably timeout anyways. Making my navigation more complex only increased each ship’s time, but at a great benefit.

**Earlier Bugs**

Since Halite has increased my skills with coding a lot, it is logical that I had many difficulties in the beginning. The most common bugs I had were:
Not sending moves to STDOUT
Due to logic structures
Sending too many moves for one ship
Due to not adding a break or continue after adding moves to moveList

**Getting Used To The Game**

This part is includes some feedback for the Halite Team. I will include this in the Feedback, this part is just for the people who are normally reading. If you are, thanks for making it to ~2100 words. A part that would have turned me away from Halite was the lack of instruction. What Halite should really have was a video like Sentdex did, in any language. Sentdex’s video helped me understand the game and how the bots worked. Without it I wouldn’t still be playing this game.

**Feedback**
This section is exclusively for the Halite team. For those interested, read on. For those not, skip on to the next section. 

As I mentioned in the previous section, if you make Halite III, and I really hope that you do, please make tutorial videos. I do like how you have the “Improve Your Basic Bot” section, but next time it would be way better if you created a video on how to actually implement these improvements. It really kicks a beginner off like me. It doesn’t need to be in any specific language either. 

It would also be great if you had a live leaderboard like mlomb. It does get tiring to click the refresh button/press f5/ press ctrl+r. It also could be nice to put a live rank on people’s profiles as well. 
	
	Not saying that Halite TV wasn’t cool, but I didn’t really use it all that much. It was slightly buggy and there wasn’t any point really. It was an awesome idea though, but I don’t think it was entirely necessary. Other than checking it out a few times I wasn’t drawn to it much.

	During this competition I’ve thought to myself many times that I would be even more motivated to participate if the tiers were larger. In my opinion, the bronze tier encompasses way too many people. I think silver should be rewarded more easily as a small reward for players. You could add a highest tier like “grandmaster” or something for truly top players. 

	A small little cosmetic problem, but silver, platinum and diamond are almost indistinguishable. I do understand the reasoning behind it and I can offer no solution but it would be great if you modified them in the future to look a bit more distinct.
 
	That being said, I still enjoyed Halite a lot. It was awesome and I loved the simplicity. Don’t lose the simplicity in Halite III! I think it’s what turned people away from BattleCode. How complex and confusing it was. I loved Halite for how it felt like a video game. It was fun, enjoyable, and felt like your everyday video game. The UI was smooth and felt easy to use and I rarely had a hard time finding anything. Thanks for a great season, and I’m looking forward to getting that T-shirt. :D

**What I Could Have Improved On**

This section will deal with what I could have improved on.

**Version Control**

Me, a naive and beginner programmer never thought version control was important until the day before finals. The date this section was written, Jan 28, I have not yet finished uploading my bots to Github, mostly because it’s a pain to do so since Github doesn’t have a desktop app for linux users which I find ironic, but you can see how I organised my bots. It’s a mess with version names and version numbers that are indescribably vague. I don’t get why I didn’t just create one bot name and stuck with it, incrementing the version numbers. Instead I had this complicated system where I stuck tags on versions such as [BUGFIX] and [FINETUNE] even though that didn’t help a single bit with version control. Due to my lack of organization, on the last day we could submit bots, I submitted a broken version that I previously thought was working fine, because I couldn’t find my best bot. The bot I uploaded stops moving once all planets are taken unless an enemy ship is within aggro range, or 35 units. I must say with some pride though, that my bugged bot has remained in the platinum tier. 

**Navigation**

I still could have improved my navigation. My navigation still avoids my own ships, even though I have taken some measures to prevent this. I was working on this the last day of submissions but I wasn’t able to get it to work. That’s why I just submitted an old bot and said: “Screw it”. I definitely could have fixed this given more time. Which segways me to my next topic:

**Time**

	I really wish I had more time on Halite. It started in October but I only started in December, resulting in only 5 weeks of time for me. I think I could have made it to the top 20 if I had started in October, but since I did not, I’m going to end up probably rank 34 or 33. As I’m writing this on January 28 12:09 PM (PST), I am rank 33 and I don’t anticipate myself going up or down anymore. To be fair, I might have been burnt out by Halite if I did start in October, and Sentdex didn’t upload his video until December, so I think I should have started earlier, but not as early as October.

**Closing Statement**

	This was one of the best experiences I’ve ever had doing anything. I’ve poured my heart and soul into my bot, and I think I achieved a good enough result for this time. My highest rank that I achieved was rank 25, and I’m proud of it. I’m not yet satisfied though. My goal for next time is to achieve Diamond tier, or top 10. I’m going to accomplish this by having better version control, starting earlier and controlling my time. These two are the most important things that I can do to ensure success for the next season of Halite and any future projects, whether that would be personal, professional or relevant at all to coding. I would like to thank the Halite team again and give my thanks to the Halite community. I look forward to Halite III with enthusiasm.

