import 'package:flutter/material.dart';

class SubjectTableWidget extends StatelessWidget {
  const SubjectTableWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(centerTitle: true, title: const Text("choose a topic")),
      body: Center(
        child: GridView.count(
          crossAxisCount: (MediaQuery.of(context).size.width ~/ 250).toInt(),
          childAspectRatio: 1,
          crossAxisSpacing: 0.0,
          mainAxisSpacing: 5,
          shrinkWrap: true,
          padding: const EdgeInsets.symmetric(horizontal: 30),
          children: [
            Card(
              child: GestureDetector(
                onTap: () => Navigator.of(context).pushNamed('/match-making'),
                child: Container(
                  height: 300,
                  decoration:
                      BoxDecoration(borderRadius: BorderRadius.circular(20)),
                  margin: const EdgeInsets.all(5),
                  padding: const EdgeInsets.all(5),
                  child: Stack(
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Expanded(
                            child: Image.asset('assets/images/match.gif'),
                          ),
                          const Row(
                            children: [
                              Text(
                                'Edit your profile',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                            ],
                          )
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
            Card(
              child: GestureDetector(
                onTap: () => Navigator.of(context).pushNamed('/test'),
                child: Container(
                  height: 300,
                  decoration:
                      BoxDecoration(borderRadius: BorderRadius.circular(20)),
                  margin: const EdgeInsets.all(5),
                  padding: const EdgeInsets.all(5),
                  child: Stack(
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Expanded(
                            child: Image.asset('assets/images/wait.gif'),
                          ),
                          const Row(
                            children: [
                              Text(
                                'Find a dance partner',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                            ],
                          )
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
