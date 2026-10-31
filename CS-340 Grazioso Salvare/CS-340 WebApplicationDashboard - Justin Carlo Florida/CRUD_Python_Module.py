# -----------------------------------------------------------------------------
# CS 340 Client/Server Development
# Project One / Project Two - CRUD Python Module
#
# Developer: Justin Carlo Florida
#
# Description:
# This module implements the Create, Read, Update, and Delete (CRUD) operations
# for the Austin Animal Center (AAC) animal shelter MongoDB database. The
# AnimalShelter class serves as the middleware between MongoDB and the
# Grazioso Salvare dashboard. It allows the client-side Dash application to
# securely connect to the database, retrieve animal records using MongoDB
# queries, and perform CRUD operations through PyMongo.
# -----------------------------------------------------------------------------

from pymongo import MongoClient


class AnimalShelter(object):
    """Provides CRUD operations for the animals collection in MongoDB."""

    def __init__(self, username, password):
        """
        Initialize the MongoDB connection using the supplied username
        and password.

        Args:
            username (str): MongoDB username.
            password (str): MongoDB password.
        """

        # MongoDB connection settings for the AAC database.
        HOST = "localhost"
        PORT = 27017
        DB = "aac"
        COL = "animals"

        # Create the MongoDB client using the supplied login credentials.
        self.client = MongoClient(
            "mongodb://%s:%s@%s:%d" % (
                username,
                password,
                HOST,
                PORT
            )
        )

        # Access the AAC database and animals collection.
        self.database = self.client[DB]
        self.collection = self.database[COL]

    def create(self, data):
        """
        Insert a new document into the animals collection.

        Args:
            data (dict): Document containing the animal data to insert.

        Returns:
            bool: True if the document was successfully inserted;
                  otherwise False.
        """

        try:
            if data:
                result = self.collection.insert_one(data)
                return result.acknowledged

            return False

        except Exception as error:
            print(f"Create operation failed: {error}")
            return False

    def read(self, query):
        """
        Retrieve documents from the animals collection that match
        the supplied MongoDB query.

        Args:
            query (dict): MongoDB query used to filter documents.

        Returns:
            list: A list containing the matching MongoDB documents.
        """

        try:
            if query is not None:
                return list(self.collection.find(query))

            return []

        except Exception as error:
            print(f"Read operation failed: {error}")
            return []

    def update(self, query, data):
        """
        Update documents in the animals collection that match
        the supplied query.

        Args:
            query (dict): MongoDB query used to select documents.
            data (dict): Fields and values to update.

        Returns:
            int: Number of documents modified.
        """

        try:
            if query and data:
                result = self.collection.update_many(
                    query,
                    {"$set": data}
                )

                return result.modified_count

            return 0

        except Exception as error:
            print(f"Update operation failed: {error}")
            return 0

    def delete(self, query):
        """
        Delete documents from the animals collection that match
        the supplied query.

        Args:
            query (dict): MongoDB query used to select documents.

        Returns:
            int: Number of documents deleted.
        """

        try:
            if query:
                result = self.collection.delete_many(query)
                return result.deleted_count

            return 0

        except Exception as error:
            print(f"Delete operation failed: {error}")
            return 0