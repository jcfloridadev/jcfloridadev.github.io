//============================================================================
// Name        : ProjectTwo.cpp
// Author      : Justin Carlo Florida
// Description : CS 300 Project Two ABCU Advising Program
//============================================================================

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <algorithm>

using namespace std;

struct Course {
    string courseNumber;
    string courseTitle;
    vector<string> prerequisites;
};

struct Node {
    Course course;
    Node* left;
    Node* right;

    Node(Course aCourse) {
        course = aCourse;
        left = nullptr;
        right = nullptr;
    }
};

class BinarySearchTree {
private:
    Node* root;

    void addNode(Node* node, Course course) {
        if (course.courseNumber < node->course.courseNumber) {
            if (node->left == nullptr) {
                node->left = new Node(course);
            }
            else {
                addNode(node->left, course);
            }
        }
        else {
            if (node->right == nullptr) {
                node->right = new Node(course);
            }
            else {
                addNode(node->right, course);
            }
        }
    }

    void inOrder(Node* node) {
        if (node == nullptr) {
            return;
        }

        inOrder(node->left);
        cout << node->course.courseNumber << ", " << node->course.courseTitle << endl;
        inOrder(node->right);
    }

    void deleteTree(Node* node) {
        if (node == nullptr) {
            return;
        }

        deleteTree(node->left);
        deleteTree(node->right);
        delete node;
    }

public:
    BinarySearchTree() {
        root = nullptr;
    }

    ~BinarySearchTree() {
        deleteTree(root);
    }

    void Insert(Course course) {
        if (root == nullptr) {
            root = new Node(course);
        }
        else {
            addNode(root, course);
        }
    }

    Course Search(string courseNumber) {
        Node* current = root;

        while (current != nullptr) {
            if (current->course.courseNumber == courseNumber) {
                return current->course;
            }

            if (courseNumber < current->course.courseNumber) {
                current = current->left;
            }
            else {
                current = current->right;
            }
        }

        Course course;
        return course;
    }

    void InOrder() {
        inOrder(root);
    }
};

// Converts course input to uppercase so searches are not case-sensitive.
string toUpperCase(string input) {
    transform(input.begin(), input.end(), input.begin(), ::toupper);
    return input;
}

vector<string> splitLine(string line) {
    vector<string> tokens;
    string token;
    stringstream ss(line);

    while (getline(ss, token, ',')) {
        tokens.push_back(token);
    }

    return tokens;
}

// Reads the course CSV file and inserts each course into the binary search tree.
void loadCourses(string fileName, BinarySearchTree* courseTree) {
    ifstream file(fileName);

    if (!file.is_open()) {
        cout << "Error: Could not open file." << endl;
        return;
    }

    string line;

    while (getline(file, line)) {
        vector<string> tokens = splitLine(line);

        if (tokens.size() < 2) {
            cout << "Error: Invalid course data format." << endl;
            continue;
        }

        Course course;
        course.courseNumber = toUpperCase(tokens[0]);
        course.courseTitle = tokens[1];

        for (unsigned int i = 2; i < tokens.size(); i++) {
            course.prerequisites.push_back(toUpperCase(tokens[i]));
        }

        courseTree->Insert(course);
    }

    file.close();

    cout << "Data loaded successfully." << endl;
}

// Prints one course and its prerequisites.
void printCourseInformation(BinarySearchTree* courseTree, string courseNumber) {
    courseNumber = toUpperCase(courseNumber);

    Course course = courseTree->Search(courseNumber);

    if (course.courseNumber.empty()) {
        cout << "Course not found." << endl;
        return;
    }

    cout << course.courseNumber << ", " << course.courseTitle << endl;

    if (course.prerequisites.empty()) {
        cout << "Prerequisites: None" << endl;
    }
    else {
        cout << "Prerequisites: ";

        for (unsigned int i = 0; i < course.prerequisites.size(); i++) {
            cout << course.prerequisites[i];

            if (i < course.prerequisites.size() - 1) {
                cout << ", ";
            }
        }

        cout << endl;
    }
}

int main() {
    BinarySearchTree* courseTree = new BinarySearchTree();
    bool dataLoaded = false;
    int choice = 0;
    string fileName;
    string courseNumber;

    cout << "Welcome to the course planner." << endl;

    while (choice != 9) {
        cout << endl;
        cout << "1. Load Data Structure." << endl;
        cout << "2. Print Course List." << endl;
        cout << "3. Print Course." << endl;
        cout << "9. Exit" << endl;
        cout << "What would you like to do? ";
        cin >> choice;

        switch (choice) {
        case 1:
            cout << "Enter the file name: ";
            cin.ignore();
            getline(cin, fileName);

            loadCourses(fileName, courseTree);
            dataLoaded = true;
            break;

        case 2:
            if (!dataLoaded) {
                cout << "Please load the data structure first." << endl;
            }
            else {
                cout << "Here is a sample schedule:" << endl;
                courseTree->InOrder();
            }
            break;

        case 3:
            if (!dataLoaded) {
                cout << "Please load the data structure first." << endl;
            }
            else {
                cout << "What course do you want to know about? ";
                cin >> courseNumber;
                printCourseInformation(courseTree, courseNumber);
            }
            break;

        case 9:
            cout << "\n=====================================================" << endl;
            cout << "\nThank you for using the ABCU Course Planner!" << endl;
            cout << "Developer: Justin Carlo Florida" << endl;
            break;

        default:
            cout << choice << " is not a valid option." << endl;
            break;
        }
    }

    delete courseTree;

    return 0;
}