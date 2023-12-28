import { Link, Outlet } from "react-router-dom";
import "./route.css";
import { ErrorPage } from "../ErrorPage.tsx";
import Contact from "./Contact.tsx";
import { Demo2x2RedBlue } from "../demos/2x2/Demo2x2RedBlue.tsx";
import { Demo2x3x2RedAndBlue } from "../demos/2x3x2/Demo2x3x2RedAndBlue.tsx";
import React from "react";

const path2x2 = "/ml/demos/2x2";
const path2x3x2 = `/ml/demos/2x3x2`;
const path3x3x3 = "/ml/demos/3x3x3";
export const routes = [
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
                path: path2x2,
                element: <Demo2x2RedBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path2x3x2,
                element: <Demo2x3x2RedAndBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path3x3x3,
                element: <Demo2x3x2RedAndBlue />,
                errorElement: <ErrorPage />,
            },
        ],
    },
];
export default function Root() {
    return (
        <>
            <div id={"wrapper"}>
                <div id="sidebar">
                    <h1>Demos side bar</h1>
                    <nav>
                        <ul>
                            <li>
                                <Link to="/">Home</Link>
                            </li>
                            <li>
                                <Link to={`/contacts/2`}>
                                    React Router Demo
                                </Link>
                            </li>
                            <li>
                                <Link to={path2x2}>2x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path2x3x2}>2x3x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path3x3x3}>
                                    3x4x3 Red/Green/Blue Demo
                                </Link>
                            </li>
                        </ul>
                    </nav>
                </div>
                <div id="detail">
                    <Outlet />
                </div>
            </div>
        </>
    );
}
