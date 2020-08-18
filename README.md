# Brain Research

## Author
Mark Chen

<a name="table"></a>
## Table of Contents
* [Introduction](#introduction)
* [Features](#features)


## Introduction
Brain Research is an Android app that studys sleep apnea, a sleep disorder in which breathing is briefly and repeatedly interrupted during sleep. It collects sleeping and breathing data from the users, blood pressure values from a blood pressure cuff, and blood oxygen level values from a pulse oximeter. 

<a href="#table">Back to Table of Contents</a>


## Features

### Pulse Oximeter Readings
The app connects to Nonin 9560 pulse oximeter via Bluetooth. Once the connection is made, the app continuously read and save the pulse and %SpO2 values measured by the device at both local storage and a Google Firebase database.

<p align="center">
	<img src="https://github.com/MarkChenTP/Brain_Research/blob/master/demo/pulseOx_app.png" height="480"/>
	<br/>
  <br/>
	<strong>Link to pulseOx demo: https://github.com/MarkChenTP/Brain_Research/raw/master/demo/pulseOx_demo_deviceShown.mp4</strong>
</p>
<br/>

### Sleep Apnea Questionnaire
The app has three sets of questions that query the user's body status and life expeience. For each study day, the user will answer the relevant questions by typing in their responses. The app save these responses at both local storage and a Google Firebase database.

<p align="center">
	<img src="https://github.com/MarkChenTP/Brain_Research/blob/master/demo/questionnaire_app.png" height="480"/>
	<br/>
  <br/>
	<strong>Link to questionnaire demo: https://github.com/MarkChenTP/Brain_Research/raw/master/demo/questionnaire_demo.mp4</strong>
</p>
<br/>

### Blood Pressure
For each study day, the user will enter their systolic pressure, diastolic pressure, and pulse values. The app save these responses at both local storage and a Google Firebase database.

<p align="center">
	<img src="https://github.com/MarkChenTP/Brain_Research/blob/master/demo/bloodPressure_app.png" height="480"/>
	<br/>
</p>
<br/>

### Valsalva and Breath Hold Challenges
The Valsalva and breath hold challenges both consist a baseline resting phase, a action phase, and a recovery resting phase. In a resting phase, the user waits and will hear a beeping sound when the countdown timer is finished. In an action phase, the user will see a progress bar that reflects the progress of the associated challenge (Valsalva: user blows air into a tube, Breath Hold: user holds the breath). 

<p align="center">
	<img src="https://github.com/MarkChenTP/Brain_Research/blob/master/demo/valsalva_breathHold_app.png" height="480"/>
	<br/>
	<br/>
	<strong>Links to valsalva demo: https://github.com/MarkChenTP/Brain_Research/raw/master/demo/valsalva_demo.mp4</strong>
  <br/>
  <strong>Links to breath hold demo: https://github.com/MarkChenTP/Brain_Research/raw/master/demo/questionnaire_demo.mp4</strong>
</p>
<br/>

### PVT Challenge
The PVT challenge asks the user to touch the red dots appear on the screen. The app save both the red dots' show-up times and the user's touch-response times at both local storage and a Google Firebase database.

<p align="center">
	<img src="https://github.com/MarkChenTP/Brain_Research/blob/master/demo/pvt_app.png" height="480"/>
	<br/>
	<br/>
	<strong>Links to PVT demo: https://github.com/MarkChenTP/Brain_Research/raw/master/demo/pvt_demo_hand.mp4</strong>
  </p>
<br/>

### Stroop Challenge
The Stroop challenge asks the user to select the color that matches the color name appearing on the screen. The app save the on-screen color's name and color, the user's elected color, and the test result at both local storage and a Google Firebase database.
<br/>

### Stress Reduction Challenge
The Stress Reduction challenge asks the user to practice a breathing exercise with an associated background music. The are two options of the breathing music for the user to select: music and guide or music only.
<br/>

<a href="#table">Back to Table of Contents</a>
