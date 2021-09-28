# Electrum api
a electrum jsonrpc wrapper used to accept Bitcoin payments with no third-party applications

Payment Processing Example: [Example](payment.md)

Send Payments Example: [Example](send_payment.md)

Original Repository: [Zaczero Electrum PHP](https://github.com/Zaczero/php-electrum-class)

# Getting started
### Installing Electrum CLI
```bash
# Download package
wget https://download.electrum.org/4.1.5/Electrum-4.1.5.tar.gz

# Extract package
tar -xvf Electrum-4.1.5.tar.gz

# Install electrum command
sudo ln -s $(pwd)/Electrum-4.1.5/run_electrum /usr/local/bin/electrum

# Check if everything works properly
electrum help
```

### Starting daemon
```bash
electrum daemon -d
```

###[Test Net]
```bash
electrum daemon -d --testnet
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
###[Test Net]
* SegWit wallet

```bash
electrum create --segwit --testnet
```

* Legacy wallet

```bash
electrum create --testnet
```


### Loading Wallet
```bash
# Load the wallet
electrum load_wallet
```
###[Test Net]
```bash
# Load the wallet
electrum load_wallet --testnet
```
