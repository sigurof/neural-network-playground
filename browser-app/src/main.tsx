import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import Root from "./routes/route.tsx";
import { ErrorPage } from "./ErrorPage.tsx";
import Contact from "./routes/Contact.tsx";
import { Demo2x2RedBlue } from "./demos/2x2/Demo2x2RedBlue.tsx";

import "./index.css";
import { Demo2x3x2RedAndBlue } from "./demos/2x3x2/Demo2x3x2RedAndBlue.tsx";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: "/contacts/:contactId",
                element: <Contact />,
            },
            {
                path: "/ml/demos/2x2",
                element: <Demo2x2RedBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: "/ml/demos/new",
                element: <Demo2x3x2RedAndBlue />,
                errorElement: <ErrorPage />,
            },
        ],
    },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <RouterProvider router={router} />
    </React.StrictMode>,
);
