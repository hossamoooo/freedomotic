/*
        This program is free software; you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation; either version 2 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program; if not, write to the Free Software
        Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
        MA 02110-1301, USA.
*/

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Original Code by AUTHOMETION S.r.l.
  Version: 1.02
  Date: 14.10.2015
  Modified by Mauro Cicolella for Freedomotic
  Date: 28.12.2015
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/****************************************************************************************
 *                                                                                      *
                                 !!!! INPORTANT NOTICE !!!
 *                                                                                      *
 ****************************************************************************************
 *                                                                                      *
   Please change the _SS_MAX_RX_BUFF value to 128 to use this sketch
   You can find _SS_MAX_RX_BUFF value in the following header file
   <Program dir>\Arduino\hardware\arduino\avr\libraries\SoftwareSerial\SoftwareSerial.h
 *                                                                                      *
 ****************************************************************************************
 *                                                                                      *
   To use LYT Shield with Arduino MEGA 2560 please connect following pins:
 *                                                                                      *
        Shield        MEGA 2560
           5              A8
          10              53
          11              51
          12              50
          13              52
 *                                                                                      *
   In JP1, JP2 and JP3 insert jumpers to left side
 *                                                                                      *
 ****************************************************************************************/

#include <SPI.h>
#include <EEPROM.h>
#include <PL1167.h>
#include <LYTWiFi.h>
#include <Messenger.h>
#include <string.h>
#include "SoftwareSerial.h"
#include <WiFiInterrupt.h>
#include <stdlib.h>

#define PL1167_CS_PIN          10 // 10 for Arduino Uno, 53 for Arduino MEGA 2560
#define ESP8266_TCP_MUX        0
#define ESP8266_TCP_SEVER_PORT 5001
#define BUFFER_LENGTH          16

SoftwareSerial mySerial(5, 6); // RX (5 for Arduino Uno / 62(A8) for Arduino Mega 2560), TX (6)
LYTWiFi myNetWork(mySerial);
//LYTWiFi myNetWork;

//Redirect serial chars from Virt to WiFi serial port and vice versa
uint8_t ui8BridgeOn = 0;

//Where commands are coming from
uint8_t ui8WhoIs = 0;

// Instantiate Messenger object with the message function and the default separator (the comma character)
Messenger SerWiFi = Messenger(',');
Messenger SerHW = Messenger(',');

char cBuf;
uint8_t ui8Counter;

// Define messenger function
void messageWiFi()
{
  char cBuffer[64];

  if (String(SerWiFi.buffer).indexOf(":") != -1)
  {
    memset(cBuffer, '\0', MESSENGERBUFFERSIZE);
    strcpy(cBuffer, (String(SerWiFi.buffer).substring(String(SerWiFi.buffer).indexOf(":") + 1, String(SerWiFi.buffer).indexOf("\r\n"))).c_str());
    strcpy(SerWiFi.buffer, cBuffer);
    ui8WhoIs = 1;
    SerHW.setBufferString(SerWiFi.readBufferString());
  }
}

// Define messenger function
void messageArduino()
{
  uint8_t ui8Counter1, ui8Counter2, ui8Total;
  uint8_t ui8MODE   = 0;
  uint8_t ui8ADDR_A = 0;
  uint8_t ui8ADDR_B = 0;
  uint8_t ui8ADDR_A1 = 0;
  uint8_t ui8ADDR_B1 = 0;
  uint8_t ui8PAR1   = 0;
  uint8_t ui8PAR2   = 0;
  uint8_t ui8PAR3   = 0;
  uint8_t ui8DIM    = 0;
  char    cBuffer1[BUFFER_LENGTH];
  char    cBuffer2[BUFFER_LENGTH];
  String  sBuffer1;
  String  sBuffer2;
  long    lBaud;

  // POWER ON LYT: PON,ADDR_A,ADDR_B,MODE
  if (SerHW.checkString("PON"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    ui8PAR1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSwitchOnAndCheck(ui8ADDR_A, ui8ADDR_B, ui8MODE, ui8PAR1) == C_ACK) {
      memcpy(cBuffer2, F(" OK!"), 4);
      readStatus();
    }
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("PON:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // POWER OFF LYT: POF,ADDR_A,ADDR_B,MODE
  else if (SerHW.checkString("POF"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    ui8PAR1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSwitchOffAndCheck(ui8ADDR_A, ui8ADDR_B, ui8MODE, ui8PAR1) == C_ACK) {
      memcpy(cBuffer2, F(" OK!"), 4);
      readStatus();
    }
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("POF:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // SET RGB VALUES: RGB,ADDR_A,ADDR_B,PAR1,PAR2,PAR3,MODE
  else if (SerHW.checkString("RGB"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8PAR1 = SerHW.readInt();
    ui8PAR2 = SerHW.readInt();
    ui8PAR3 = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    ui8DIM = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSetRGBValuesAndCheck(ui8ADDR_A, ui8ADDR_B, ui8PAR1, ui8PAR2, ui8PAR3, ui8MODE, ui8DIM) == C_ACK) {
      memcpy(cBuffer2, F(" OK!"), 4);
      readStatus();
    }
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("RGB:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // SET BRIGHTNESS VALUE: SBR,ADDA,ADDB,PAR1,MODE,DIM
  else if (SerHW.checkString("SBR"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8PAR2 = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    ui8DIM = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSetBrightnessValueAndCheck(ui8ADDR_A, ui8ADDR_B, ui8PAR2, ui8MODE, ui8DIM) == C_ACK) {
      memcpy(cBuffer2, F(" OK!"), 4);
      readStatus();
    }
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("SBR:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // SET NEW ADDRESS: SEA,ADDR_A,ADDR_B,ADDR_A1,ADDR_B1
  else if (SerHW.checkString("SEA"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8ADDR_A1 = SerHW.readInt();
    ui8ADDR_B1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fAddLampAddressAndCheck(ui8ADDR_A, ui8ADDR_B, ui8ADDR_A1, ui8ADDR_B1) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("SEA: "), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // DELETE ADDRESS: DEA,ADDR_A,ADDR_B,ADDR_A1,ADDR_B1
  else if (SerHW.checkString("DEA"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8ADDR_A1 = SerHW.readInt();
    ui8ADDR_B1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fDeleteLampAddressAndCheck(ui8ADDR_A, ui8ADDR_B, ui8ADDR_A1, ui8ADDR_B1) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("DEA:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // RESET BULB: RES
  else if (SerHW.checkString("RES"))
  {
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fResetLampAddressAndCheck() == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("RES:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // READ ADDRESS TABLE: RAT,ADDR_A,ADDR_B
  else if (SerHW.checkString("RAT"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fReadLampAddressTableAndCheck(ui8ADDR_A, ui8ADDR_B) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    if (ui8WhoIs == 1)
    {
      for (ui8Counter1 = 0; ui8Counter1 < PROTOCOL_ADDRESS_TABLE_POSITIONS; ui8Counter1++)
      {
        memset(cBuffer1, '\0', BUFFER_LENGTH);
        sprintf(cBuffer1, "(%2d,%2d)", myNetWork.LampAddressTable.ui8AddressTable[ui8Counter1][0], myNetWork.LampAddressTable.ui8AddressTable[ui8Counter1][1]);
        myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
        delay(100);
      }
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("RAT:"), 4);
      strcat(cBuffer1, cBuffer2);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    }
    else
    {
      for (ui8Counter1 = 0; ui8Counter1 < PROTOCOL_ADDRESS_TABLE_POSITIONS; ui8Counter1++)
      {
        Serial.print(F("("));
        Serial.print(myNetWork.LampAddressTable.ui8AddressTable[ui8Counter1][0], DEC);
        Serial.print(F(", "));
        Serial.print(myNetWork.LampAddressTable.ui8AddressTable[ui8Counter1][1], DEC);
        Serial.print(F(") "));
      }
      Serial.println();
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("RAT:"), 4);
      strcat(cBuffer1, cBuffer2);
      Serial.println(cBuffer1);
    }
  }
  // SAVE CURRENT STATE OF BULB: SAV,ADDR_A,ADDR_B,
  else if (SerHW.checkString("SAV"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSaveLampSettingsAndCheck(ui8ADDR_A, ui8ADDR_B) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("SAV:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // READ INFO STATUS: RIS,ADDR_A,ADDR_B
  else if (SerHW.checkString("RIS"))
  {
    readStatus();
  }
  // SET SYNC WORD: SSW,ADDR_A,ADDR_B,ADDR_A1,ADDR_B1
  else if (SerHW.checkString("SSW"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8ADDR_A1 = SerHW.readInt();
    ui8ADDR_B1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSetSyncWordAndCheck(ui8ADDR_A, ui8ADDR_B, ui8ADDR_A1, ui8ADDR_B1) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("SSW:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // SET LOCAL SYNC WORD: SLSW,ADDR_A,ADDR_B,MODE
  else if (SerHW.checkString("SLSW"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    myNetWork.vfSetLocalSyncWord(ui8ADDR_A, ui8ADDR_B, ui8MODE);
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("SLSW!"), 5);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 5);
    }
    else
      Serial.println(F("SLSW!"));
  }
  // SET RGB VALUE IN FAST MODE: FRGB,ADDR_A,ADDR_B,PAR1,PAR2,PAR3
  else if (SerHW.checkString("FRGB"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8PAR1 = SerHW.readInt();
    ui8PAR2 = SerHW.readInt();
    ui8PAR3 = SerHW.readInt();
    while (Serial.available())
      Serial.read();
    while (Serial.available() == 0)
    {
      myNetWork.ui8fSetRGBValuesAndCheck(ui8ADDR_A, ui8ADDR_B, ui8PAR1, 0, 0, C_MULTICAST);
      delay(100);
      myNetWork.ui8fSetRGBValuesAndCheck(ui8ADDR_A, ui8ADDR_B, 0, ui8PAR2, 0, C_MULTICAST);
      delay(100);
      myNetWork.ui8fSetRGBValuesAndCheck(ui8ADDR_A, ui8ADDR_B, 0, 0, ui8PAR3, C_MULTICAST);
      delay(100);
    }
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("FRGB!"), 5);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 5);
    }
    else
      Serial.println(F("FRGB!"));
  }
  // SET LOCAL RADIO CHANNEL: SLRC,CHAN,MODE
  else if (SerHW.checkString("SLRC"))
  {
    ui8PAR1 = SerHW.readInt();
    ui8MODE = SerHW.readInt();
    myNetWork.vfSetLocalChannel(ui8PAR1, ui8MODE);
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("SLRC!"), 5);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 5);
    }
    else
      Serial.println(F("SLRC!"));
  }
  // SET RADIO CHANNEL OF LYT SHIELD: SRC,ADDR_A,ADDR_B,CHAN
  else if (SerHW.checkString("SRC"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8PAR1 = SerHW.readInt();
    memset(cBuffer2, '\0', BUFFER_LENGTH);
    if (myNetWork.ui8fSetChannelAndCheck(ui8ADDR_A, ui8ADDR_B, ui8PAR1) == C_ACK)
      memcpy(cBuffer2, F(" OK!"), 4);
    else
      memcpy(cBuffer2, F(" KO!"), 4);
    memset(cBuffer1, '\0', BUFFER_LENGTH);
    memcpy(cBuffer1, F("SRC:"), 4);
    strcat(cBuffer1, cBuffer2);
    if (ui8WhoIs == 1)
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
    else
      Serial.println(cBuffer1);
  }
  // TEST RADIO CHANNEL EFFICENCY: TEST,ADDR_A,ADDR_B,CHAN
  else if (SerHW.checkString("TEST"))
  {
    ui8ADDR_A = SerHW.readInt();
    ui8ADDR_B = SerHW.readInt();
    ui8Counter1 = SerHW.readInt();
    while (Serial.available())
      Serial.read();
    while ((Serial.available() == 0) && (ui8Counter1 <= 127))
    {
      ui8Counter2 = 0;
      while ((myNetWork.ui8fSetChannelAndCheck(ui8ADDR_A, ui8ADDR_B, ui8Counter1) != C_ACK) && (ui8Counter2 < 3))
      {
        delay(1000);
        ui8Counter2++;
      }
      if (ui8Counter2 < 3)
      {
        ui8Total = 0;
        for (ui8Counter2 = 0; ui8Counter2 < 20; ui8Counter2++)
        {
          myNetWork.ui8fSetRGBValuesAndCheck(ui8ADDR_A, ui8ADDR_B, 10 * ui8Counter2, 0, 0, C_UNICAST);
          ui8Total += myNetWork.ui8SentCommandCounterMem;
          delay(500);
          if (Serial.available() != 0)
            break;
        }
        Serial.print("Channel: ");
        Serial.print(ui8Counter1);
        Serial.print(", Total number of sent commands: ");
        Serial.println(ui8Total);
        ui8Counter1++;
      }
      else
        break;
    }
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("TEST!"), 5);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 5);
    }
    else
      Serial.println(F("TEST!"));
  }
  // ACTVATE BRIDGE between Serial and Virtual serial: BRIDGE
  else if (SerHW.checkString("BRIDGE"))
  {
    ui8BridgeOn = 1;
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("BRIDGE!"), 7);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 7);
    }
    else
      Serial.println(F("BRIDGE!"));
  }
  // Discover all available WiFi routers: LAP
  else if (SerHW.checkString("LAP"))
  {
    if (ui8WhoIs == 0)
    {
      Serial.println(myNetWork.sfGetAPList_ESP8266());
      Serial.println(F("LAP!"));
    }
  }
  // Join ESP8266: JOI,SSID,PWD
  else if (SerHW.checkString("JOI"))
  {
    if (ui8WhoIs == 0)
    {
      if (myNetWork.bfSetOprToStationSoftAP_ESP8266())
      {
        myNetWork.bfRestart_ESP8266();
        SerHW.copyString(cBuffer1, BUFFER_LENGTH);
        SerHW.copyString(cBuffer2, BUFFER_LENGTH);
        if (myNetWork.bfJoinAP_ESP8266(String(cBuffer1), String(cBuffer2)))
        {
          Serial.println(F("Join: OK"));
          delay(4000);
          myNetWork.vfGetLocalIP_ESP8266(sBuffer1, sBuffer2);
          Serial.print(F("Local IP Address :"));
          Serial.println(sBuffer1);
          Serial.print(F("Local Mac Address:"));
          Serial.println(sBuffer2);
          delay(2000);
          if (myNetWork.bfEnableMUX_ESP8266())
          {
            delay(2000);
            if (myNetWork.bfStartTCPServer_ESP8266(ESP8266_TCP_SEVER_PORT))
              Serial.println(F("TCP Server: OK"));
            else
              Serial.println(F("TCP Server: FAIL"));
          }
          else
            Serial.println(F("TCP Server: FAIL"));
        }
        else
          Serial.println(F("Join: FAIL"));
      }
      else
        Serial.println(F("FAIL: ESP8266 not configured"));
      Serial.println(F("JOI!"));
    }
  }
  // Start a ESP8266 TCP server: STCP
  else if (SerHW.checkString("STCP"))
  {
    if (ui8WhoIs == 0)
    {
      delay(2000);
      myNetWork.vfGetLocalIP_ESP8266(sBuffer1, sBuffer2);
      Serial.print(F("Local IP Address :"));
      Serial.println(sBuffer1);
      Serial.print(F("Local Mac Address:"));
      Serial.println(sBuffer2);
      delay(2000);
      if (atoi(sBuffer1.c_str()) > 0)
      {
        if (myNetWork.bfEnableMUX_ESP8266())
        {
          delay(2000);
          if (myNetWork.bfStartTCPServer_ESP8266(ESP8266_TCP_SEVER_PORT))
            Serial.println(F("TCP Server: OK"));
          else
            Serial.println(F("TCP Server: FAIL"));
        }
        else
          Serial.println(F("TCP Server: FAIL"));
      }
      else
        Serial.println(F("FAIL: ESP8266 not connected to network"));
      Serial.println(F("STCP!"));
    }
  }
  // Read IP and Mac Addresses of ESP8266: RIP
  else if (SerHW.checkString("RIP"))
  {
    if (ui8WhoIs == 0)
    {
      myNetWork.vfGetLocalIP_ESP8266(sBuffer1, sBuffer2);
      Serial.print(F("Local IP Address :"));
      Serial.println(sBuffer1);
      Serial.print(F("Local Mac Address:"));
      Serial.println(sBuffer2);
      Serial.println(F("RIP!"));
    }
  }
  // Change Serial port baud rate (***SE ARRIVA DA WIFI CHE SUCCEDE***)
  else if (SerHW.checkString("UART"))
  {
    lBaud = SerHW.readLong();
    Serial.end();
    delay(200);
    Serial.begin(lBaud);
  }
  else if (SerHW.checkString("WDG"))
  {

  }
  else
  {
    if (ui8WhoIs == 1)
    {
      memset(cBuffer1, '\0', BUFFER_LENGTH);
      memcpy(cBuffer1, F("UNK!"), 4);
      myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, 4);
    }
    else
      Serial.println(F("UNK!"));
  }
  ui8WhoIs = 0;
}

void setup()
{
  // Initialize Serial Communications
  mySerial.begin(9600);
  Serial.begin(9600, SERIAL_8N1);

  myNetWork.vfInitialize(PL1167_CS_PIN);
  //  vfISRInit(&myNetWork);
  vfISRInit();

  SerWiFi.attach(messageWiFi);
  SerHW.attach(messageArduino);
  delay(500);
}

void readStatus()
{
  uint8_t ui8Counter1, ui8Counter2, ui8Total;
  uint8_t ui8MODE   = 0;
  uint8_t ui8ADDR_A = 0;
  uint8_t ui8ADDR_B = 0;
  uint8_t ui8ADDR_A1 = 0;
  uint8_t ui8ADDR_B1 = 0;
  uint8_t ui8PAR1   = 0;
  uint8_t ui8PAR2   = 0;
  uint8_t ui8PAR3   = 0;
  uint8_t ui8DIM    = 0;
  char    cBuffer1[BUFFER_LENGTH];
  char    cBuffer2[BUFFER_LENGTH];
  String  sBuffer1;

  ui8ADDR_A = SerHW.readInt();
  ui8ADDR_B = SerHW.readInt();
  if (ui8WhoIs == 1)
    myNetWork.bfSend_ESP8266(ESP8266_TCP_MUX, (const unsigned char*)cBuffer1, strlen(cBuffer1));
  else
  {
    Serial.print(F("STATUS:"));
    Serial.print(ui8ADDR_A);
    Serial.print(F(","));
    Serial.print(ui8ADDR_B);
    Serial.print(F(" "));
    for (ui8Counter1 = 0; ui8Counter1 < PROTOCOL_ANSWER_LENGHT; ui8Counter1++)
    {
      Serial.print(myNetWork.ReceivedAnswer.AnswerStruct.ui8Answer[ui8Counter1], DEC);
      Serial.print(F(" "));
    }
    Serial.println();
  }
}

void loop()
{
  while (Serial.available())
  {
    if (ui8BridgeOn == 0)
      SerHW.process(Serial.read());
    else
    {
      cBuf = Serial.read();
      if (cBuf == '!')
        ui8BridgeOn = 0;
      else
        mySerial.print(cBuf);
    }
  }
  while (mySerial.available())
  {
    if (ui8BridgeOn == 0)
      SerWiFi.process(mySerial.read());
    else
    {
      cBuf = mySerial.read();
      if (cBuf == '!')
        ui8BridgeOn = 0;
      else
        Serial.print(cBuf);
    }
  }
}
