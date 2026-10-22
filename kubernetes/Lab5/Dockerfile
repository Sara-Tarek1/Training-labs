
FROM node:26-alpine

WORKDIR /app

RUN apk upgrade --no-cache

COPY package.json package-lock.json ./

RUN npm ci --omit=dev \
    && rm -rf /usr/local/lib/node_modules/npm /usr/local/bin/npm /usr/local/bin/npx /root/.npm

COPY app.js ./

USER node

EXPOSE 3000

CMD ["node", "app.js"]

