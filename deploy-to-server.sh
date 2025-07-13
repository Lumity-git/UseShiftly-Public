#!/bin/bash
# Deploy local Scheduling-Project to remote SSH server

REMOTE_USER="l"
REMOTE_HOST="70.120.241.113"
REMOTE_PORT=2222
REMOTE_DIR="~/Scheduling-Project"
LOCAL_DIR="$(pwd)"



echo "🔄 Syncing $LOCAL_DIR to $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR ..."

rsync -avz --delete --exclude 'target/' --exclude '.git/' -e "ssh -p $REMOTE_PORT" "$LOCAL_DIR/" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR"

if [ $? -eq 0 ]; then
  echo "✅ Deployment complete!"
else
  echo "❌ Deployment failed. Check your SSH credentials and network."
fi
