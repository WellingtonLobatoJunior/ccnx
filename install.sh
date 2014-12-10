#!/bin/sh
#Script desenvolvido pelo aluno de engenharia da computação Wellington Viana Lobato Junior, o script realiza a instalação do CCNx de uma maneira mais fácil e simples
#Script developed by computer engineering student Wellington Viana Lobato Junior, the script performs the installation of CCNx an easy and simple way
#Para executar esse arquivo, basta dar a permissão com o chmod +x install.sh e executar com o ./install.sh
#To run this file, simply give permission with chmod + x install.sh and run with the ./install.sh
#Arquivos necessarios para instalar o CCNx
#Necessary files to install CCNx
sudo apt-get install update
sudo apt-get install ant -y
sudo apt-get install autoconf -y
sudo apt-get install libssl-dev -y 
sudo apt-get install libexpat1-dev -y 
sudo apt-get install libexpat-ocaml-dev -y
sudo apt-get install libpcap-dev -y
sudo apt-get install libecryptfs0 -y
sudo apt-get install libxml2-utils -y
sudo apt-get install automake -y
sudo apt-get install gawk -y
sudo apt-get install gcc -y
sudo apt-get install g++ -y
sudo apt-get install git-core -y
sudo apt-get install pkg-config -y
sudo apt-get install libpcre3-dev -y
sudo apt-get install openjdk-6-jre-lib -y
sudo apt-get install openjdk-6-jdk -y #Caso a versao do CCNx precise usar o jdk6(If the version of CCNx need to use the JDK6)
sudo apt-get install openjdk-7-jdk -y #Caso a versao do CCNx precise usar o jdk7(If the version of CCNx need to use the JDK7)

#Definindo a a variavel JAVA_HOME
#Setting the variable JAVA_HOME
echo "JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/bin" >> .bashrc
echo "export JAVA_HOME " >> .bashrc
echo "PATH=$PATH:$JAVA_HOME" >> .bashrc
echo "export PATH" >> .bashrc


#Procedimentos para a instalacao do CCNx
#Procedures for the installation of CCNx
wget http://www.ccnx.org/releases/ccnx-0.8.1.tar.gz
tar -vzxf ccnx-0.8.1.tar.gz
cd ccnx-0.8.1
sudo ./configure
sudo make
sudo make install
