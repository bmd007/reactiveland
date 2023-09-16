import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';

void main() {
  runApp(const MyApp());
}

FutureBuilder<String> displayMarkdown(BuildContext context, String file) {
  return FutureBuilder(
    future: DefaultAssetBundle.of(context).loadString("assets/" + file),
    builder: (BuildContext context, AsyncSnapshot<String> snapshot) {
      if (snapshot.hasData) {
        return Markdown(
          data: snapshot.data!,
        );
      }
      return const Center(
        child: CircularProgressIndicator(),
      );
    },
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      initialRoute: '/',
      routes: {
        '/': (context) =>
            displayMarkdown(context, 'event-driven-match-making.md'),
        '/overview': (context) => const Text('overview'),
      },
      title: 'Reactiveland blog: Mahdi',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
    );
  }
}
