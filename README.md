# Fingerprint Plugin for Flutter

[![pub package](https://img.shields.io/pub/v/fingerprint_plugin.svg)](https://pub.dev/packages/fingerprint_plugin)

A Flutter plugin that provides biometric authentication with automatic fingerprint detection for Android.

## Features

- Automatic fingerprint detection on app launch
- No button press required
- Real-time authentication feedback
- Simple and intuitive API
- Supports Android's biometric authentication

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  fingerprint_plugin: ^1.0.0
```

## Usage

### Import the package

```dart
import 'package:fingerprint_plugin/fingerprint_plugin.dart';
```

### Check biometric availability

```dart
bool isAvailable = await FingerprintPlugin.checkBiometrics();
```

### Start authentication

```dart
bool isAuthenticated = await FingerprintPlugin.authenticate();
```

## Example

```dart
import 'package:flutter/material.dart';
import 'package:fingerprint_plugin/fingerprint_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool _isLoading = true;
  String _statusMessage = 'Initializing...';
  bool _isAuthenticated = false;

  @override
  void initState() {
    super.initState();
    _startFingerprintDetection();
  }

  Future<void> _startFingerprintDetection() async {
    try {
      final isAvailable = await FingerprintPlugin.checkBiometrics();
      
      if (!isAvailable) {
        setState(() {
          _statusMessage = 'Biometric sensor not available';
          _isLoading = false;
        });
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = 'Place your finger on the sensor';
      });

      while (mounted) {
        try {
          final isAuthenticated = await FingerprintPlugin.authenticate();
          
          setState(() {
            _isAuthenticated = isAuthenticated;
            _statusMessage = isAuthenticated 
                ? '✅ Authentication successful!' 
                : '❌ Fingerprint not recognized';
          });
          
          if (isAuthenticated) {
            await Future.delayed(const Duration(seconds: 2));
          }
          
          if (mounted) {
            setState(() {
              _statusMessage = 'Place your finger on the sensor';
              _isAuthenticated = false;
            });
          }
        } catch (e) {
          if (mounted) {
            setState(() {
              _statusMessage = 'Error: ${e.toString()}';
            });
          }
          await Future.delayed(const Duration(seconds: 1));
        }
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Error accessing sensor: ${e.toString()}';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              _isAuthenticated ? Icons.verified : Icons.fingerprint,
              size: 80,
              color: _isLoading 
                  ? Colors.grey
                  : _isAuthenticated 
                      ? Colors.green
                      : Colors.blue,
            ),
            const SizedBox(height: 24),
            Text(
              _statusMessage,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            if (_isLoading) ...[
              const SizedBox(height: 24),
              const CircularProgressIndicator(),
            ],
          ],
        ),
      ),
    );
  }
}
```

## Android Setup

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```
