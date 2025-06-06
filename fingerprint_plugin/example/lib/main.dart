import 'package:flutter/material.dart';
import 'dart:async';
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
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool _isLoading = true;
  String _statusMessage = 'Placez votre doigt sur le capteur';
  bool _isAuthenticated = false;
  Timer? _retryTimer;

  @override
  void initState() {
    super.initState();
    _startFingerprintDetection();
  }

  @override
  void dispose() {
    _retryTimer?.cancel();
    super.dispose();
  }

  Future<void> _startFingerprintDetection() async {
    try {
      print('Démarrage de la détection d\'empreinte...');
      
      // Vérifier d'abord si la biométrie est disponible
      print('Vérification de la disponibilité biométrique...');
      final isBiometricAvailable = await FingerprintPlugin.checkBiometrics().catchError((e) {
        print('Erreur lors de la vérification biométrique: $e');
        return false;
      });
      
      print('Disponibilité biométrique: $isBiometricAvailable');
      
      if (!isBiometricAvailable) {
        print('Aucun capteur biométrique disponible');
        setState(() {
          _statusMessage = 'Capteur biométrique non disponible';
          _isLoading = false;
        });
        return;
      }

      setState(() {
        _isLoading = false;
        _statusMessage = 'Placez votre doigt sur le capteur';
      });

      while (mounted) {
        try {
          print('Démarrage de l\'authentification...');
          final isAuthenticated = await FingerprintPlugin.authenticate().catchError((e) {
            print('Erreur lors de l\'authentification: $e');
            return false;
          });
          
          print('Résultat de l\'authentification: $isAuthenticated');
          
          // Mettre à jour l'interface utilisateur avec le résultat
          setState(() {
            _isAuthenticated = isAuthenticated;
            _statusMessage = isAuthenticated 
                ? '✅ Authentification réussie !' 
                : '❌ Empreinte non reconnue';
          });
          
          // Si l'authentification a réussi, on attend un peu avant de redémarrer
          if (isAuthenticated) {
            await Future.delayed(const Duration(seconds: 2));
          }
          
          // Réinitialiser pour la prochaine tentative
          if (mounted) {
            setState(() {
              _statusMessage = 'Placez votre doigt sur le capteur';
              _isAuthenticated = false;
            });
          }
          
        } catch (e) {
          // En cas d'erreur, on attend un peu avant de réessayer
          if (mounted) {
            setState(() {
              _statusMessage = 'Erreur de détection';
            });
          }
          await Future.delayed(const Duration(seconds: 1));
        }
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Erreur d\'accès au capteur';
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
            // Icône d'empreinte digitale
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
            // Message d'état
            Text(
              _statusMessage,
              style: TextStyle(
                fontSize: 18,
                color: _isLoading 
                    ? Colors.grey
                    : _isAuthenticated 
                        ? Colors.green
                        : Colors.blue,
                fontWeight: FontWeight.bold,
              ),
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
