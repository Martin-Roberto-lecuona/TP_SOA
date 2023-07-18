# Introducción
Las funcionalidades consisten básicamente en tres modos principales, el modo “Security cam” en donde el 
dispositivo móvil es trasladado de un lado hacia el otro, como si de una cámara de seguridad se tratase.
El modo “Manual” el cual permite al usuario mover su celular mediante dos botones, seleccionando el lado 
hacia el cual desee desplazar.
Y el tercer modo, “Influencer”, consiste en que el sistema, haciendo uso de sus sensores, pueda mover el 
dispositivo móvil del usuario en base al movimiento físico de este último, con la finalidad de que la cámara de 
su dispositivo nunca deje de “seguir” al usuario que está utilizando.
Además, cada uno de estos modos tiene la posibilidad de activar o desactivar la luz automática, permitiendo en 
caso de que el mismo esté activado se va a alterar la intensidad de las luces led en función de la luz ambiental. O 
por el contrario el usuario puede elegir el modo de luz manual en donde puede simplemente prender o apagar la 
luz.
# Parte Fisica
[URL proyecto Tinkercad](https://www.tinkercad.com/things/cqaytCsPfah-copy-of-modulo-servo-y-led-con-maquina-de-estado/editel?sharecode=8f4_bV9s8l6c5raXJ6Ii_b8664BFapBSHQtiW2O95Rk)
# Diagrama de estados
![image](https://github.com/Martin-Roberto-lecuona/TP_SOA/assets/79217570/d3fad1d2-a371-4443-a698-16ff7ed0dc13)
# Manual de usuario del sistema embebido
## Modos de Uso
En todos los modos se permite manejar las luces manualmente o mediante sensores de luz que varían 
inversamente proporcionales a la intensidad de luz ambiental de las luces. El botón “LED_MODE” irá 
desde Luz Ambiental → Prendido → Apagado de manera cíclica. Lo mismo sucede en el “SERVO_MODE” 
que irá desde Influencer → Manual → SecurityCam.
Además de esto está el “SWITCH_ON_OFF_SERVO” que permite de manera segura alimentar el servomotor, 
para su correcto funcionamiento.
### Influencer
Este modo de uso está preparado para que el usuario encastre su smartphone al case y que éste se mueva 
automáticamente. Hace un seguimiento del objeto más próximo, mediante los 2 sensores inferiores que tiene el 
case (no tapar). Tiene una distancia máxima de trabajo (150 cm) por lo que un objeto muy lejano no los tendrá 
en cuenta. 
### Manual
Este modo de uso está preparado para que el usuario encastre su smartphone al case y que éste se mueva a gusto 
del usuario de manera manual. Al presionar los botones “SLEFT_BUTTON” y “SRIGHT_BUTTON” hará que 
se mueva el case hacia la izquierda o hacia la derecha respectivamente. Tiene un límite de giro de 90º a la 
derecha e izquierda.
### Security Cam
Este modo de uso está preparado para que el usuario encastre su smartphone o cámara web al case y que éste se 
mueva de izquierda a derecha haciendo un barrido de 180º.
# Manual de usuario del sistema android 
Aquí se mostrará como usar la aplicación desde el teléfono android con nuestra aplicación. Dicha aplicación esta 
en inglés para facilitar la internacionalización del producto.
### Primera pantalla
![image](https://github.com/Martin-Roberto-lecuona/TP_SOA/assets/79217570/9aca3e95-53a7-428f-b4a1-aff22c6f0ec0)
En esta primera pantalla podemos ver la lista de dispositivos que previamente 
fueron conectados al teléfono, luego al presionar “Connect to Arduino” se 
redireccionará a la siguiente pantalla y se conectará directamente al Bluetooth 
integrado en el producto.
### Segunda pantalla
![image](https://github.com/Martin-Roberto-lecuona/TP_SOA/assets/79217570/50a7671f-df78-4091-a32b-e96998a28fc7)
En esta pantalla se pueden ver 2 botones que permiten cambiar el modo del 
embebido o el modo de las luces, ambos con un texto que indicará en qué modo 
está actualmente. Luego arriba a la derecha se puede observar un botón para 
recargar estos textos, esto esta solo para posibles fallas del sistema y no debería 
ser necesario usarlo de manera continua debido a que la actualización se hace 
automáticamente. Por último están los botones con flechas, estos solo están 
disponibles y visibles cuando esté en modo manual para mover de izquierda a 
derecha la cámara o teléfono celular en el case
