# Electrum api
a electrum cli wrapper used to accept Bitcoin payments with no third-party applications
[Zaczero Electrum PHP](https://github.com/Zaczero/php-electrum-class)

# Getting started
### Installing Electrum CLI
```bash
# Download package
wget https://download.electrum.org/3.3.8/Electrum-3.3.8.tar.gz

# Extract package
tar -xvf Electrum-3.3.8.tar.gz

# Install electrum command
sudo ln -s $(pwd)/Electrum-3.3.8/run_electrum /usr/local/bin/electrum

# Check if everything works properly
electrum help
```

### Configuring RPC

```bash
electrum setconfig rpcuser "user"
electrum setconfig rpcpassword "S3CR3T_password"
electrum setconfig rpcport 7777
```

### Creating wallet

* SegWit wallet

```bash
electrum create --segwit
```

* Legacy wallet

```bash
electrum create
```

### Starting Electrum in daemon mode

```bash
# Start the daemon
electrum daemon start

# Load the wallet
electrum daemon load_wallet
```
