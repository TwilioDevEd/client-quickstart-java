FROM maven:3-openjdk-11

WORKDIR /usr/app/

RUN apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y build-essential

COPY . .

# No need to update these values, only at docker-compose.yml
# If the application can't read those values, you can try update them here as well
ENV TWILIO_ACCOUNT_SID=your_account_sid
ENV TWILIO_CALLER_ID=+1XXXYYYZZZZ
ENV TWILIO_TWIML_APP_SID=APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
ENV API_KEY=SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
ENV API_SECRET=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

RUN make install

EXPOSE 8080

CMD ["make", "serve"]
