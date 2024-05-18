FROM oven/bun:1.1
COPY . .
RUN bun install
RUN bun run build
EXPOSE 3000
ENV NODE_ENV=production
CMD ["bun", "express-entry.ts"]
