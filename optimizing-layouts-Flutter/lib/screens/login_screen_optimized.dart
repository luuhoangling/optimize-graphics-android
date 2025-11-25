import 'dart:convert';

import 'package:android_basic/constants.dart';
import 'package:android_basic/screens/home_screen.dart';
import 'package:android_basic/screens/signup_screen.dart';
import 'package:android_basic/widgets/custom_button.dart';
import 'package:android_basic/widgets/custom_widgets.dart';
import 'package:android_basic/widgets/simple_toast.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import '../config/server.dart';

class LoginScreenOptimized extends StatefulWidget {
  const LoginScreenOptimized({super.key});

  @override
  State<LoginScreenOptimized> createState() => _LoginScreenOptimizedState();
}

class _LoginScreenOptimizedState extends State<LoginScreenOptimized> {
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();
  static const _storage = FlutterSecureStorage();
  bool isLoading = false;

  void handleLogin() async {
    if (usernameController.text.trim().isEmpty || passwordController.text.trim().isEmpty) {
      SimpleToast.showError(context, 'Vui lòng nhập đầy đủ thông tin!');
      return;
    }

    setState(() {
      isLoading = true;
    });

    final url = Uri.parse('$baseUrl/api/auth/user/login');

    final body = jsonEncode({
      'username': usernameController.text.trim(),
      'password': passwordController.text.trim(),
    });

    try {
      final response = await http.post(
        url,
        headers: {'Content-Type': 'application/json'},
        body: body,
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);

        final token = data['token'];
        final user = data['user'];

        // ✅ Lưu token sau khi đăng nhập
        await _storage.write(key: 'jwt_token', value: token);

        // Lưu id, họ tên user
        await _storage.write(key: 'user', value: jsonEncode(user));
        
        // Hiển thị thông báo thành công
        SimpleToast.showSuccess(context, 'Đăng nhập thành công! Chào mừng bạn quay trở lại!');
        
        // Chuyển màn hình sau 1.5 giây
        Future.delayed(const Duration(milliseconds: 1500), () {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const HomeScreen()),
          );
        });
      } else {
        final errorData = jsonDecode(response.body);
        String errorMessage = 'Đăng nhập thất bại!';
        
        if (errorData['message'] != null) {
          errorMessage = errorData['message'];
        }
        
        SimpleToast.showError(context, errorMessage);
      }
    } catch (e) {
      SimpleToast.showError(context, 'Không thể kết nối đến server. Vui lòng thử lại!');
    } finally {
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          const _BackgroundDecoration(),
          SafeArea(
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 22),
                child: Column(
                  children: [
                    const SizedBox(height: 100),
                    const _LoginHeader(),
                    const SizedBox(height: 120),
                    _LoginForm(
                      usernameController: usernameController,
                      passwordController: passwordController,
                      isLoading: isLoading,
                      onLogin: handleLogin,
                    ),
                    const SizedBox(height: 40),
                    const _CreateAccountButton(),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _BackgroundDecoration extends StatelessWidget {
  const _BackgroundDecoration();

  @override
  Widget build(BuildContext context) {
    return const Stack(
      children: [
        Positioned(
          top: -330,
          right: -330,
          child: _CircleDecoration(
            size: 600,
            isFilled: true,
          ),
        ),
        Positioned(
          top: -125, // -((1 / 4) * 500)
          right: -112.5, // -((1 / 4) * 500)
          child: _CircleDecoration(
            size: 450,
            isFilled: false,
          ),
        ),
      ],
    );
  }
}

class _CircleDecoration extends StatelessWidget {
  const _CircleDecoration({
    required this.size,
    required this.isFilled,
  });

  final double size;
  final bool isFilled;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: size,
      width: size,
      decoration: BoxDecoration(
        color: isFilled ? lightBlue : null,
        shape: BoxShape.circle,
        border: isFilled ? null : Border.all(color: lightBlue, width: 2),
      ),
    );
  }
}

class _LoginHeader extends StatelessWidget {
  const _LoginHeader();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text("Login here", style: h2),
        const SizedBox(height: 10),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 50),
          child: Text(
            "Wellcome back",
            style: h2.copyWith(fontSize: 18, color: black),
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }
}

class _LoginForm extends StatelessWidget {
  const _LoginForm({
    required this.usernameController,
    required this.passwordController,
    required this.isLoading,
    required this.onLogin,
  });

  final TextEditingController usernameController;
  final TextEditingController passwordController;
  final bool isLoading;
  final VoidCallback onLogin;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        CustomTextfield(
          hint: "Username",
          controller: usernameController,
        ),
        const SizedBox(height: 20),
        CustomTextfield(
          hint: "Password",
          controller: passwordController,
          obscureText: true,
        ),
        const SizedBox(height: 25),
        const _ForgotPasswordButton(),
        const SizedBox(height: 30),
        _LoginButton(
          isLoading: isLoading,
          onPressed: onLogin,
        ),
      ],
    );
  }
}

class _ForgotPasswordButton extends StatelessWidget {
  const _ForgotPasswordButton();

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: Alignment.centerRight,
      child: Text(
        "Forgot your password",
        style: body.copyWith(
          fontSize: 16,
          color: primary,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

class _LoginButton extends StatelessWidget {
  const _LoginButton({
    required this.isLoading,
    required this.onPressed,
  });

  final bool isLoading;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return CustomButton(
      text: isLoading ? "Đang đăng nhập..." : "Sign in",
      isLarge: true,
      onPressed: isLoading ? null : onPressed,
    );
  }
}

class _CreateAccountButton extends StatelessWidget {
  const _CreateAccountButton();

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => const SignupScreen(),
          ),
        );
      },
      child: Text(
        "Create new account",
        style: body.copyWith(
          fontSize: 16,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}